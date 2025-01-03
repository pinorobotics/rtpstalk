/*
 * Copyright 2022 pinorobotics
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
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
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
import java.util.function.Predicate;
import pinorobotics.rtpstalk.WriterSettings;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.behavior.writer.RtpsDataMessageBuilder;
import pinorobotics.rtpstalk.impl.behavior.writer.RtpsHeartbeatMessageBuilder;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.qos.WriterQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.structure.history.CacheChange;
import pinorobotics.rtpstalk.impl.spec.structure.history.HistoryCache;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;
import pinorobotics.rtpstalk.metrics.RtpsTalkMetrics;

/**
 * Statefull RTPS writer with reliable reliability {@link ReliabilityQosPolicy.Kind#RELIABLE}.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class StatefullReliableRtpsWriter<D extends RtpsTalkMessage> extends RtpsWriter<D>
        implements Runnable, AutoCloseable {

    private final Meter METER =
            GlobalOpenTelemetry.getMeter(StatefullReliableRtpsWriter.class.getSimpleName());
    private final LongCounter HEARTBEATS_METER =
            METER.counterBuilder(RtpsTalkMetrics.HEARTBEATS_COUNT_METRIC)
                    .setDescription(RtpsTalkMetrics.HEARTBEATS_COUNT_METRIC_DESCRIPTION)
                    .build();

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

    private HistoryCache<D> historyCache;
    private int heartbeatCount = 1;
    private DataChannelFactory channelFactory;
    private LocalOperatingEntities operatingEntities;
    private int historyCacheMaxSize;
    private WriterRtpsReader<D> writerReader;
    private WriterQosPolicySet qosPolicy;
    private WriterSettings writerSettings;
    private boolean isClosed;

    public StatefullReliableRtpsWriter(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            DataChannelFactory channelFactory,
            LocalOperatingEntities operatingEntities,
            EntityId writerEntiyId,
            WriterQosPolicySet qosPolicy,
            WriterSettings writerSettings) {
        super(config, tracingToken, publisherExecutor, writerEntiyId);
        this.writerSettings = writerSettings;
        Preconditions.equals(qosPolicy.reliabilityKind(), ReliabilityQosPolicy.Kind.RELIABLE);
        this.channelFactory = channelFactory;
        this.operatingEntities = operatingEntities;
        this.heartbeatPeriod = config.publicConfig().heartbeatPeriod();
        this.historyCacheMaxSize = config.publicConfig().historyCacheMaxSize();
        this.historyCache = new HistoryCache<>(getTracingToken());
        operatingEntities.getLocalWriters().add(this);
        // for heartbeat purposes (to process ackNacks) we create reader
        writerReader = new WriterRtpsReader<>(getTracingToken(), this);
        this.qosPolicy = qosPolicy;
    }

    /** Contains the history of CacheChange changes for this RTPS Writer. */
    public HistoryCache<D> getWriterCache() {
        return historyCache;
    }

    @Override
    public long newChange(D data) {
        var seqNum = super.newChange(data);
        logger.fine("New change submitted");
        historyCache.addChange(new CacheChange<>(getGuid(), getLastChangeNumber(), data));
        return seqNum;
    }

    public synchronized void matchedReaderAdd(
            Guid remoteReaderGuid, List<Locator> unicast, ReaderQosPolicySet qosPolicy)
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
                        channelFactory.connect(getTracingToken(), unicast),
                        remoteReaderGuid,
                        getGuid().entityId);
        var reliabilityKind = qosPolicy.reliabilityKind();
        var proxy =
                switch (reliabilityKind) {
                    case RELIABLE ->
                            new ReliableReaderProxy(remoteReaderGuid, unicast, sender, qosPolicy);
                    case BEST_EFFORT ->
                            new BestEffortReaderProxy(remoteReaderGuid, unicast, sender, qosPolicy);
                    default ->
                            throw new UnsupportedOperationException(
                                    "ReliabilityQosPolicy " + reliabilityKind);
                };
        replayHistoryCacheIfNeeded(proxy);
        logger.fine(
                "Adding reader proxy for reader with guid {0} and reliability {1}",
                proxy.getRemoteReaderGuid(), reliabilityKind);
        var numOfReaders = matchedReaders.size();
        matchedReaders.put(proxy.getRemoteReaderGuid(), proxy);
        subscribe(proxy.getSender());
        if (numOfReaders == 0) {
            executor.scheduleWithFixedDelay(
                    this, 0, heartbeatPeriod.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private void replayHistoryCacheIfNeeded(ReaderProxy proxy) {
        if (qosPolicy.durabilityKind() != DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS)
            return;
        if (proxy.getQosPolicy().reliabilityKind() != ReliabilityQosPolicy.Kind.BEST_EFFORT) return;
        logger.fine("Replaying history changes for Reader {0}", proxy.getRemoteReaderGuid());
        var builder =
                new RtpsDataMessageBuilder(
                        getConfig(),
                        getTracingToken(),
                        getGuid().guidPrefix,
                        proxy.getRemoteReaderGuid().guidPrefix);
        historyCache
                .getAllSortedBySeqNum(getGuid())
                .forEach(
                        change -> {
                            builder.add(change.getSequenceNumber(), change.getDataValue());
                        });
        proxy.getSender().replay(builder);
    }

    public boolean matchedReaderRemove(Guid remoteGuid) {
        var reader = matchedReaders.remove(remoteGuid);
        if (reader == null) {
            logger.fine("Trying to remove unknown matched reader {0}, ignoring...", remoteGuid);
            return false;
        } else {
            reader.close();
            cleanupCacheAndRequest();
            logger.fine("Matched reader {0} is removed", remoteGuid);
            return true;
        }
    }

    public void matchedReadersRemove(GuidPrefix guidPrefix) {
        logger.fine("Removing all matched readers with guidPrefix {0}", guidPrefix);
        var count =
                matchedReaders.keySet().stream()
                        .filter(guid -> guid.guidPrefix.equals(guidPrefix))
                        .filter(this::matchedReaderRemove)
                        .count();
        logger.fine("Removed {0} matched readers with guidPrefix {1}", count, guidPrefix);
    }

    private int matchedReadersRemove(Predicate<ReaderProxy> filter) {
        var bestEffortReaders =
                matchedReaders.entrySet().stream()
                        .filter(e -> filter.test(e.getValue()))
                        .map(Map.Entry::getKey)
                        .toList();
        bestEffortReaders.forEach(this::matchedReaderRemove);
        logger.fine("Removed {0} matched readers", bestEffortReaders.size());
        return bestEffortReaders.size();
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
            sendHeartbeats();
            cleanupReaders();
            cleanupCacheAndRequest();
        } catch (Exception e) {
            logger.severe("Writer heartbeat error", e);
        }
    }

    private void cleanupReaders() {
        if (isClosed) {
            var count =
                    matchedReadersRemove(
                            reader -> reader.getHighestAckedSeqNum() == getLastChangeNumber());
            if (count > 0)
                logger.fine("Removed {0} matched readers as they received all the changes", count);
        }
    }

    /**
     * Publisher sends changes only when the Reader requests them. Once cache is full the Reader
     * stops requesting new changes. Each time we clean up the cache we need to request publisher
     * for a new changes, otherwise we may end up waiting for changes forever.
     */
    protected void cleanupCacheAndRequest() {
        if (qosPolicy.durabilityKind() == DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS) {
            if (!isClosed) return;
            // On close we are not going to accept new Readers therefore
            // we can start cleanup the cache. That way we make sure that current
            // Readers receive everything what was published in the cache.
        }
        if (matchedReaders.isEmpty()) {
            logger.fine("Cleaning up all changes since there is no matched readers available");
            historyCache.removeAllBelow(getLastChangeNumber());
        } else {
            var oldestSeqNum =
                    matchedReaders.values().stream()
                            .mapToLong(ReaderProxy::getHighestAckedSeqNum)
                            .min()
                            .orElse(0);
            // we delete only what all readers acked, if any of the
            // readers did not acked anything we return
            if (oldestSeqNum == 0) return;
            if (oldestSeqNum == Long.MAX_VALUE) oldestSeqNum = getLastChangeNumber();
            logger.fine(
                    "Cleaning up all changes up to and including {0} since they are acknowledged by"
                            + " all the readers",
                    oldestSeqNum);
            historyCache.removeAllBelow(oldestSeqNum + 1);
        }
        request();
    }

    @Override
    public void close() {
        if (isClosed) return;
        logger.fine("Closing");
        cancelSubscription();
        isClosed = true;
        var numOfPendingChanges = historyCache.getNumberOfChanges(getGuid());
        while (numOfPendingChanges != 0) {
            if (matchedReaders.isEmpty()) {
                logger.fine(
                        "Discarding pending changes since there is no matched readers available");
                break;
            }
            var nonBestEffortReaders =
                    matchedReaders.entrySet().stream()
                            .filter(
                                    e ->
                                            ReaderProxy.IS_BEST_ERRORT_FILTER
                                                    .negate()
                                                    .test(e.getValue()))
                            .map(Map.Entry::getKey)
                            .toList();
            if (nonBestEffortReaders.isEmpty()) {
                var count = matchedReadersRemove(ReaderProxy.IS_BEST_ERRORT_FILTER);
                logger.fine("Removed {0} BEST_EFFORT matched readers", count);
                logger.fine(
                        "Discarding pending changes since there is no matched readers available");
                break;
            }
            logger.fine(
                    "Waiting for {0} pending changes [{1}..{2}] in the history cache to be sent to"
                            + " the following readers {3}",
                    numOfPendingChanges,
                    historyCache.getSeqNumMin(getGuid()),
                    historyCache.getSeqNumMax(getGuid()),
                    nonBestEffortReaders);
            XThread.sleep(heartbeatPeriod.toMillis());
            numOfPendingChanges = historyCache.getNumberOfChanges(getGuid());
        }
        super.close();
        operatingEntities.getLocalWriters().remove(getGuid().entityId);
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
        if (writerSettings.pushMode()) {
            super.sendLastChangeToAllReaders();
        }
        /**
         * For reliable Writer we send changes only when Reader notifies that it lost them (through
         * heartbeat-acknack interaction).
         *
         * <p>If we would be sending changes immediately + by request, it can lead to messages being
         * sent out-of-order:
         *
         * <ul>
         *   <li>Changes in cache [c1, c2, c3] with the corresponding {@link Data#writerSN} -
         *       Received acknack from Reader saying that it lost [c1, c2, c3]
         *   <li>New change c4 was published and immediately sent to Reader
         *   <li>Lost changes [c1, c2, c3] sent to Reader
         * </ul>
         *
         * <p>In this case Reader will receive messages out-of-order [c4, c1, c2, c3] and since c4
         * will have greater {@link Data#writerSN} all previous will not be processed.
         */
        matchedReaders.values().stream()
                .filter(ReaderProxy.IS_BEST_ERRORT_FILTER)
                .map(
                        readerProxy ->
                                new RtpsDataMessageBuilder(
                                                getConfig(),
                                                getTracingToken(),
                                                getGuid().guidPrefix,
                                                readerProxy.getRemoteReaderGuid().guidPrefix)
                                        .addAll(getLastMessage()))
                .forEach(this::submit);
    }

    private void sendHeartbeats() {
        var seqNumMin = historyCache.getSeqNumMin(getGuid());
        if (seqNumMin <= 0) {
            logger.fine("Skipping heartbeat since history cache is empty");
            return;
        }
        var seqNumMax = historyCache.getSeqNumMax(getGuid());
        Preconditions.isLess(0, seqNumMax, "Negative sequence number");
        var readers = matchedReaders.values();
        if (readers.isEmpty()) {
            logger.fine("Skipping heartbeat since there is no readers available");
            return;
        }
        readers.stream()
                .filter(ReaderProxy.IS_RELIABLE_FILTER)
                .map(readerProxy -> readerProxy.getRemoteReaderGuid().guidPrefix)
                .map(
                        readerGuidPrefix ->
                                new RtpsHeartbeatMessageBuilder(
                                        getGuid().guidPrefix,
                                        readerGuidPrefix,
                                        seqNumMin,
                                        seqNumMax,
                                        heartbeatCount))
                .forEach(this::submit);
        logger.fine("Heartbeat {0} submitted to {1} readers", heartbeatCount, readers.size());
        heartbeatCount++;
        HEARTBEATS_METER.add(1);
    }

    private void sendRequested() {
        matchedReaders.values().stream().forEach(this::sendRequested);
    }

    private void sendRequested(ReaderProxy readerProxy) {
        var requestedChanges = readerProxy.requestedChanges();
        var remoteReaderGuid = readerProxy.getRemoteReaderGuid();
        if (requestedChanges.isEmpty()) {
            logger.fine(
                    "Nothing to submit for reader {0} as it did not request any changes, ignoring",
                    remoteReaderGuid);
            return;
        }
        var builder =
                new RtpsDataMessageBuilder(
                        getConfig(),
                        getTracingToken(),
                        getGuid().guidPrefix,
                        remoteReaderGuid.guidPrefix);
        historyCache
                .findAll(getGuid(), requestedChanges)
                .forEach(change -> builder.add(change.getSequenceNumber(), change.getDataValue()));
        var numOfFoundChanges = builder.getDataCount();
        if (numOfFoundChanges == 0) {
            logger.fine("No requested changes were found for reader {0}", remoteReaderGuid);
            return;
        }

        // changes requested by one matched Reader we submit to all the senders of matched Readers
        // of the current Writer
        // this requires each sender to filter the changes before sending them
        submit(builder);
        logger.fine(
                "Submitted {0} out of {1} requested changes to reader {2}",
                numOfFoundChanges, requestedChanges.size(), remoteReaderGuid);
    }

    public Subscriber<RtpsMessage> getWriterReader() {
        return writerReader;
    }
}
