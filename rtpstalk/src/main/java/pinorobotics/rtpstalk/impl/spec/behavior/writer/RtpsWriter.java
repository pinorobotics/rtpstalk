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
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.RtpsTalkMetrics;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.behavior.writer.RtpsDataMessageBuilder;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.structure.RtpsEntity;
import pinorobotics.rtpstalk.impl.spec.structure.history.HistoryCache;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * Abstract implementation of the Writer.
 *
 * <p>This writer does not use {@link HistoryCache} and caches only last change which it sends to
 * readers directly.
 *
 * <p>Data flow:
 *
 * <pre>{@code
 * USER calls {@link RtpsWriter#newChange} or USER publisher to which this writer subscribed issues {@link RtpsWriter#onNext(Object)}:
 * - {@link RtpsWriter} publishes change to all its connected subscribers (endpoint readers):
 *  - {@link RtpsMessageSender} sends message to remote reader1
 *  - {@link RtpsMessageSender} sends message to remote reader2
 *  - ...
 * }</pre>
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public abstract class RtpsWriter<D extends RtpsTalkMessage>
        extends SubmissionPublisher<RtpsMessageSender.MessageBuilder>
        implements Subscriber<D>, RtpsEntity, AutoCloseable {
    private final Meter METER = GlobalOpenTelemetry.getMeter(RtpsWriter.class.getSimpleName());
    private final LongHistogram SUBMITTED_CHANGES_METER =
            METER.histogramBuilder(RtpsTalkMetrics.SUBMITTED_CHANGES_METRIC)
                    .setDescription(RtpsTalkMetrics.SUBMITTED_CHANGES_METRIC_DESCRIPTION)
                    .ofLongs()
                    .build();
    protected final XLogger logger;
    private RtpsTalkConfigurationInternal config;

    private long lastChangeNumber;
    private Guid writerGuid;
    private RtpsDataMessageBuilder lastMessage;
    private Optional<Subscription> subscriptionOpt = Optional.empty();

    private TracingToken tracingToken;

    protected RtpsWriter(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            EntityId writerEntiyId) {
        this(config, tracingToken, publisherExecutor, writerEntiyId, true);
    }

    /**
     * @param pushMode Note that for a {@link ReliabilityQosPolicy.Kind#BEST_EFFORT} Writer,
     *     pushMode is true, as there are no acknowledgments.
     */
    @RtpsSpecReference(
            paragraph = "8.4.9.1.1",
            protocolVersion = Predefined.Version_2_3,
            text = "Writer always pushes out data as it becomes available")
    protected RtpsWriter(
            RtpsTalkConfigurationInternal config,
            TracingToken token,
            Executor publisherExecutor,
            EntityId writerEntityId,
            boolean pushMode) {
        super(publisherExecutor, config.publicConfig().publisherMaxBufferSize());
        this.config = config;
        this.tracingToken = new TracingToken(token, writerEntityId.toString());
        this.writerGuid = new Guid(config.publicConfig().guidPrefix(), writerEntityId);
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    /**
     * Internal counter used to assign increasing sequence number to each change made by the Writer.
     */
    protected long getLastChangeNumber() {
        return lastChangeNumber;
    }

    protected RtpsDataMessageBuilder getLastMessage() {
        return lastMessage;
    }

    public void repeatLastChange() {
        Preconditions.notNull(lastMessage);
        logger.entering("repeatLastChange");
        submit(lastMessage);
        logger.exiting("repeatLastChange");
    }

    public long newChange(D data) {
        logger.entering("newChange");
        SUBMITTED_CHANGES_METER.record(1);
        lastChangeNumber++;
        lastMessage = new RtpsDataMessageBuilder(config, writerGuid.guidPrefix);
        lastMessage.add(lastChangeNumber, data);
        sendLastChangeToAllReaders();
        logger.exiting("newChange");
        return lastChangeNumber;
    }

    protected void request() {
        subscriptionOpt.ifPresent(
                sub -> {
                    logger.fine("Requesting next message from the local publisher");
                    sub.request(1);
                });
    }

    protected void sendLastChangeToAllReaders() {
        submit(lastMessage);
    }

    @Override
    public Guid getGuid() {
        return writerGuid;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        Preconditions.isTrue(this.subscriptionOpt.isEmpty(), "Already subscribed");
        this.subscriptionOpt = Optional.of(subscription);
        subscription.request(1);
    }

    @Override
    public void onNext(D item) {
        newChange(item);
        request();
    }

    @Override
    public void onError(Throwable throwable) {
        logger.severe(throwable);
    }

    @Override
    public void onComplete() {
        logger.fine("Publisher stopped");
    }

    @Override
    public void close() {
        cancelSubscription();
        super.close();
        logger.fine("Closed");
    }

    protected RtpsTalkConfigurationInternal getConfig() {
        return config;
    }

    /**
     * Unsubscribe from the publisher this writer subscribed to. Mostly the publisher is user
     * publisher which emits changes to be sent by the writer.
     */
    protected void cancelSubscription() {
        subscriptionOpt.ifPresent(Subscription::cancel);
        subscriptionOpt = Optional.empty();
    }

    public TracingToken getTracingToken() {
        return tracingToken;
    }
}
