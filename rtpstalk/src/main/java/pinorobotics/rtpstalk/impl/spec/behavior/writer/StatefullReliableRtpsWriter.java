/*
 * Copyright 2022 rtpstalk project
 * 
 * Website: https://github.com/pinorobotics/rtpstalk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pinorobotics.rtpstalk.impl.spec.behavior.writer;

import id.xfunction.Preconditions;
import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.lang.XThread;
import id.xfunction.logging.TracingToken;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.behavior.writer.RtpsDataMessageBuilder;
import pinorobotics.rtpstalk.impl.behavior.writer.RtpsHeartbeatMessageBuilder;
import pinorobotics.rtpstalk.impl.qos.DurabilityKind;
import pinorobotics.rtpstalk.impl.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.impl.qos.ReliabilityKind;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.structure.history.CacheChange;
import pinorobotics.rtpstalk.impl.spec.structure.history.HistoryCache;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender.MessageBuilder;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * Statefull RTPS writer with reliable reliability {@link ReliabilityQosPolicy.Kind#RELIABLE}.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class StatefullReliableRtpsWriter<D extends RtpsTalkMessage> extends RtpsWriter<D>
        implements Runnable, AutoCloseable {

    private ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(
                    new NamedThreadFactory("SpdpBuiltinParticipantWriter"));

    /** Used to maintain state on the remote Readers matched up with this Writer. */
    private Map<Guid, ReaderProxy> matchedReaders = new ConcurrentHashMap<>();

    /**
     * Protocol tuning parameter that allows the RTPS Writer to repeatedly announce the availability
     * of data by sending a Heartbeat Message.
     */
    private Duration heartbeatPeriod;

    private HistoryCache<D> historyCache = new HistoryCache<>();
    private int heartbeatCount;
    private DataChannelFactory channelFactory;
    private OperatingEntities operatingEntities;
    private int historyCacheMaxSize;
    private WriterRtpsReader<D> writerReader;
    private PublisherQosPolicy qosPolicy;

    private boolean isClosed;

    public StatefullReliableRtpsWriter(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            DataChannelFactory channelFactory,
            OperatingEntities operatingEntities,
            EntityId writerEntiyId,
            PublisherQosPolicy qosPolicy) {
        super(config, tracingToken, publisherExecutor, writerEntiyId, true);
        Preconditions.isTrue(
                qosPolicy.reliabilityKind() == ReliabilityKind.RELIABLE,
                "ReliabilityKind not supported: " + qosPolicy.reliabilityKind());
        this.channelFactory = channelFactory;
        this.operatingEntities = operatingEntities;
        this.heartbeatPeriod = config.heartbeatPeriod();
        this.historyCacheMaxSize = config.historyCacheMaxSize();
        operatingEntities.getWriters().add(this);
        // for heartbeat purposes (to process ackNacks) we create reader
        writerReader = new WriterRtpsReader<>(getTracingToken(), this);
        this.qosPolicy = qosPolicy;
    }

    /** Contains the history of CacheChange changes for this RTPS Writer. */
    public HistoryCache<D> getWriterCache() {
        return historyCache;
    }

    @Override
    public void newChange(D data) {
        super.newChange(data);
        logger.fine("New change submitted");
        historyCache.addChange(new CacheChange<>(getGuid(), getLastChangeNumber(), data));
    }

    public synchronized void matchedReaderAdd(Guid remoteReaderGuid, List<Locator> unicast)
            throws IOException {
        if (matchedReaders.containsKey(remoteReaderGuid)) {
            logger.fine(
                    "Reader {0} is already registered with the writer, not adding it",
                    remoteReaderGuid);
            return;
        }
        var sender =
                new RtpsMessageSender(
                        getTracingToken(),
                        channelFactory.connect(getTracingToken(), unicast.get(0)),
                        remoteReaderGuid,
                        getGuid().entityId);
        var proxy = new ReaderProxy(remoteReaderGuid, unicast, sender);
        logger.fine("Adding reader proxy for reader with guid {0}", proxy.getRemoteReaderGuid());
        var numOfReaders = matchedReaders.size();
        matchedReaders.put(proxy.getRemoteReaderGuid(), proxy);
        subscribe(proxy.getSender());
        if (numOfReaders == 0) {
            executor.scheduleWithFixedDelay(
                    this, 0, heartbeatPeriod.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public void matchedReaderRemove(Guid remoteGuid) {
        var reader = matchedReaders.remove(remoteGuid);
        if (reader == null) {
            logger.fine("Trying to remove unknown matched reader {0}, ignoring...", remoteGuid);
        } else {
            reader.close();
            cleanupCache();
            logger.fine("Matched reader {0} is removed", remoteGuid);
        }
    }

    /** This operation finds the {@link ReaderProxy} with given {@link Guid} */
    public Optional<ReaderProxy> matchedReaderLookup(Guid guid) {
        return Optional.ofNullable(matchedReaders.get(guid));
    }

    @Override
    public void run() {
        try {
            if (executor.isShutdown()) return;
            sendRequested();
            sendHeartbeat();
            cleanupCache();
        } catch (Exception e) {
            logger.severe("Writer heartbeat error", e);
        }
    }

    private void cleanupCache() {
        if (qosPolicy.durabilityKind() == DurabilityKind.TRANSIENT_LOCAL_DURABILITY_QOS) {
            if (!isClosed) return;
            // On close we are not going to accept new Readers therefore
            // we can start cleanup the cache. That way we make sure that current
            // Readers receive everything what was published in the cache.
        }
        var oldestSeqNum =
                matchedReaders.values().stream()
                        .mapToLong(ReaderProxy::getHighestSeqNumSent)
                        .min()
                        .orElse(0);
        // we delete only what all readers acked, if any of the
        // readers did not acked anything we return
        if (oldestSeqNum == 0) return;
        historyCache.removeAllBelow(oldestSeqNum);
        request();
    }

    @Override
    public void close() {
        cancelSubscription();
        isClosed = true;
        var numOfPendingChanges = historyCache.getNumberOfChanges(getGuid());
        while (numOfPendingChanges != 0) {
            if (matchedReaders.isEmpty()) {
                logger.fine(
                        "Discarding pending changes since there is no matched readers available");
                break;
            }
            logger.fine(
                    "Waiting for {0} pending changes in the history cache to be sent",
                    numOfPendingChanges);
            XThread.sleep(heartbeatPeriod.toMillis());
            numOfPendingChanges = historyCache.getNumberOfChanges(getGuid());
        }
        super.close();
        operatingEntities.getWriters().remove(getGuid().entityId);
        executor.shutdown();
        writerReader.getSubscription().ifPresent(Subscription::cancel);
    }

    @Override
    protected void request() {
        if (historyCache.getNumberOfChanges(getGuid()) < historyCacheMaxSize) super.request();
    }

    @RtpsSpecReference(
            paragraph = "8.4.2.2",
            protocolVersion = Predefined.Version_2_3,
            text = "Writers must not send data out-of-order")
    @Override
    protected void sendLastChangeToAllReaders() {
        /**
         * For reliable Writer we send changes only when Reader notifies that it lost them (through
         * heartbeat-acknack interaction).
         *
         * <p>If we would be sending changes immediately + by request, it can lead to messages being
         * sent out-of-order:
         *
         * <p>- Changes in cache [c1, c2, c3] with the corresponding {@link Data#writerSN} -
         * Received acknack from Reader saying that it lost [c1, c2, c3] - New change c4 was
         * published and immediately sent to Reader - Lost changes [c1, c2, c3] sent to Reader
         *
         * <p>In this case Reader will receive messages out-of-order [c4, c1, c2, c3] and since c4
         * will have greater {@link Data#writerSN} all previous will not be processed.
         */
    }

    private void sendHeartbeat() {
        nextHeartbeatMessage()
                .ifPresent(
                        message -> {
                            submit(message);
                            logger.fine("Heartbeat submitted");
                        });
    }

    private Optional<MessageBuilder> nextHeartbeatMessage() {
        var seqNumMin = historyCache.getSeqNumMin(getGuid());
        if (seqNumMin <= 0) {
            logger.fine("Skipping heartbeat since history cache is empty");
            return Optional.empty();
        }
        var seqNumMax = historyCache.getSeqNumMax(getGuid());
        Preconditions.isLess(0, seqNumMax, "Negative sequence number");
        var heartbeat =
                new RtpsHeartbeatMessageBuilder(
                        getGuid().guidPrefix, seqNumMin, seqNumMax, heartbeatCount++);
        return Optional.of(heartbeat);
    }

    private void sendRequested() {
        matchedReaders.values().stream().forEach(this::sendRequested);
    }

    private void sendRequested(ReaderProxy readerProxy) {
        var requestedChanges = readerProxy.requestedChanges();
        if (requestedChanges.isEmpty()) return;
        var builder =
                new RtpsDataMessageBuilder(
                        getConfig(),
                        getGuid().guidPrefix,
                        readerProxy.getRemoteReaderGuid().guidPrefix);
        historyCache
                .findAll(getGuid(), requestedChanges)
                .forEach(change -> builder.add(change.getSequenceNumber(), change.getDataValue()));
        if (!builder.hasData()) return;
        // all interested ReaderProxy subscribed to this writer
        submit(builder);
        logger.fine("Submitted {0} requested changes", requestedChanges.size());
    }

    public Subscriber<RtpsMessage> getWriterReader() {
        return writerReader;
    }
}
