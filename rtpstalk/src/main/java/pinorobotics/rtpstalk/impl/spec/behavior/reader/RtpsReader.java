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
package pinorobotics.rtpstalk.impl.spec.behavior.reader;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsDataPackager;
import pinorobotics.rtpstalk.impl.RtpsDataPackager.MessageTypeMismatchException;
import pinorobotics.rtpstalk.impl.behavior.reader.DataFragmentReaderProcessor;
import pinorobotics.rtpstalk.impl.behavior.reader.FilterByEntityIdRtpsSubmessageVisitor;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.walk.Result;
import pinorobotics.rtpstalk.impl.spec.messages.walk.RtpsSubmessageVisitor;
import pinorobotics.rtpstalk.impl.spec.messages.walk.RtpsSubmessagesWalker;
import pinorobotics.rtpstalk.impl.spec.structure.RtpsEntity;
import pinorobotics.rtpstalk.impl.spec.structure.history.CacheChange;
import pinorobotics.rtpstalk.impl.spec.structure.history.HistoryCache;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiver;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;
import pinorobotics.rtpstalk.metrics.RtpsTalkMetrics;

/**
 * Base RTPS Reader implementation.
 *
 * <p>Each reader can be subscribed to only one {@link RtpsMessageReceiver}. And to one {@link
 * RtpsMessageReceiver} can be subscribed multiple different readers (many endpoints to one
 * receiver).
 *
 * <pre>{@code
 * USER subscribes to:
 * - {@link RtpsReader} subscribes to:
 *  - {@link RtpsMessageReceiver} receives messages from single data channel
 *   - remote writerX endpoint1
 *   - remote writerX endpoint2
 *   - ...
 *
 * }</pre>
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsReader<D extends RtpsTalkMessage> extends SubmissionPublisher<D>
        implements RtpsEntity, Subscriber<RtpsMessage>, RtpsSubmessageVisitor {

    private final Meter METER = GlobalOpenTelemetry.getMeter(RtpsReader.class.getSimpleName());
    private final LongHistogram PROCESS_TIME_METER =
            METER.histogramBuilder(RtpsTalkMetrics.PROCESS_TIME_METRIC)
                    .setDescription(RtpsTalkMetrics.PROCESS_TIME_METRIC_DESCRIPTION)
                    .ofLongs()
                    .build();
    private final LongCounter DATA_METER =
            METER.counterBuilder(RtpsTalkMetrics.DATA_COUNT_METRIC)
                    .setDescription(RtpsTalkMetrics.DATA_COUNT_METRIC_DESCRIPTION)
                    .build();
    private final LongCounter RTPS_READER_COUNT_METER =
            METER.counterBuilder(RtpsTalkMetrics.RTPS_READER_COUNT_METRIC)
                    .setDescription(RtpsTalkMetrics.RTPS_READER_COUNT_METRIC_DESCRIPTION)
                    .build();

    protected final XLogger logger;

    private HistoryCache<D> cache;
    private RtpsSubmessagesWalker walker = new RtpsSubmessagesWalker();
    private Guid guid;
    private RtpsSubmessageVisitor filterVisitor;
    private ReliabilityQosPolicy.Kind reliabilityKind;
    private Optional<Subscription> subscription = Optional.empty();
    private TracingToken tracingToken;
    private RtpsDataPackager<D> packager = new RtpsDataPackager<>();
    private Class<D> messageType;
    private DataFragmentReaderProcessor processor;

    public RtpsReader(
            RtpsTalkConfiguration config,
            TracingToken token,
            Class<D> messageType,
            Executor publisherExecutor,
            Guid readerGuid,
            ReliabilityQosPolicy.Kind reliabilityKind) {
        super(publisherExecutor, config.publisherMaxBufferSize());
        this.messageType = messageType;
        this.tracingToken = new TracingToken(token, "r", readerGuid.entityId.toString());
        this.guid = readerGuid;
        this.reliabilityKind = reliabilityKind;
        this.cache = new HistoryCache<>(tracingToken);
        processor = new DataFragmentReaderProcessor(tracingToken);
        filterVisitor = new FilterByEntityIdRtpsSubmessageVisitor(readerGuid.entityId, this);
        logger = XLogger.getLogger(getClass(), tracingToken);
        RTPS_READER_COUNT_METER.add(1);
    }

    /** Contains the history of CacheChange changes for this RTPS Reader. */
    public HistoryCache<D> getReaderCache() {
        return cache;
    }

    @Override
    public Guid getGuid() {
        return guid;
    }

    public ReliabilityQosPolicy.Kind getReliabilityKind() {
        return reliabilityKind;
    }

    @Override
    public Result onData(GuidPrefix guidPrefix, Data d) {
        DATA_METER.add(1);
        logger.fine("Received data {0}", d);
        var writerGuid = new Guid(guidPrefix, d.writerId);
        try {
            packager.extractMessage(messageType, d)
                    .ifPresent(
                            message -> {
                                d.inlineQos.ifPresent(
                                        inlineQos ->
                                                processInlineQos(
                                                        writerGuid,
                                                        d.writerSN,
                                                        message,
                                                        inlineQos));
                                addChange(new CacheChange<>(writerGuid, d.writerSN.value, message));
                            });
        } catch (MessageTypeMismatchException e) {
            if (Objects.equals(d.readerId, EntityId.Predefined.ENTITYID_UNKNOWN.getValue()))
                logger.fine(
                        "Mismatch between message types. Message directed to reader"
                            + " ENTITYID_UNKNOWN but since current reader message type differs, it"
                            + " is ignored: {0}",
                        e.getMessage());
            else logger.warning("Mismatch between message types, message will be ignored: {0}", e);
        }

        return Result.CONTINUE;
    }

    @Override
    public Result onDataFrag(GuidPrefix guidPrefix, DataFrag dataFrag) {
        DATA_METER.add(1);
        var writerGuid = new Guid(guidPrefix, dataFrag.writerId);
        processor
                .addDataFrag(writerGuid, dataFrag)
                .ifPresent(
                        message -> {
                            addChangeInternal(
                                    new CacheChange<>(
                                            writerGuid, dataFrag.writerSN.value, (D) message));
                        });
        return Result.CONTINUE;
    }

    private void addChangeInternal(CacheChange<D> cacheChange) {
        logger.entering("addChangeInternal");
        if (isClosed()) {
            logger.fine("Reader is closed, ignoring the change");
            return;
        }
        addChange(cacheChange);
        logger.exiting("addChangeInternal");
    }

    /**
     * @return HistoryCache#addChange(CacheChange)
     */
    @RtpsSpecReference(
            paragraph = "8.4.1.1.9.b",
            protocolVersion = Predefined.Version_2_3,
            text =
                    """
                    For a BEST_EFFORT DDS DataReader, changes in its RTPS Reader's HistoryCache are made visible to the user
                    only if no future changes have already been made visible (i.e., if there are no changes in the RTPS Receiver's
                    HistoryCache with a higher sequence number).
                    """)
    protected boolean addChange(CacheChange<D> cacheChange) {
        logger.entering("addChange");
        var seqNumMax = cache.getSeqNumMax(cacheChange.getWriterGuid());
        var isAdded = cache.addChange(cacheChange);
        if (isAdded && seqNumMax < cacheChange.getSequenceNumber()) {
            submitChangeToUser(cacheChange);
        }
        logger.exiting("addChange");
        return isAdded;
    }

    protected void submitChangeToUser(CacheChange<D> cacheChange) {
        logger.fine(
                "Submitting new change with sequence number {0} from the writer {1} to user"
                        + " subscribers",
                cacheChange.getSequenceNumber(), cacheChange.getWriterGuid());
        submit(cacheChange.getDataValue());
    }

    protected void process(RtpsMessage message) {
        walker.walk(message, filterVisitor);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        Preconditions.notNull(subscription, "Already subscribed");
        this.subscription = Optional.of(subscription);
        subscription.request(1);
    }

    @Override
    public void onNext(RtpsMessage message) {
        var startAt = Instant.now();
        try {
            process(message);
        } catch (Exception e) {
            logger.severe(e);
        } finally {
            PROCESS_TIME_METER.record(Duration.between(startAt, Instant.now()).toMillis());
            subscription.get().request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        logger.severe(throwable);
        throw new UnsupportedOperationException();
    }

    @Override
    public void onComplete() {}

    protected void processInlineQos(
            Guid writer, SequenceNumber seqNum, D message, ParameterList inlineQos) {
        logger.fine("Ignoring inlineQos");
    }

    @Override
    public String toString() {
        return tracingToken.toString();
    }

    public TracingToken getTracingToken() {
        return tracingToken;
    }

    @Override
    public void close() {
        subscription.ifPresent(Subscription::cancel);
        super.close();
        logger.fine("Closed");
    }
}
