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
package pinorobotics.rtpstalk.behavior.writer;

import id.xfunction.Preconditions;
import id.xfunction.logging.XLogger;
import java.util.Optional;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.RtpsDataMessageBuilder;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.structure.RtpsEntity;
import pinorobotics.rtpstalk.structure.history.HistoryCache;
import pinorobotics.rtpstalk.transport.RtpsMessageSender;

/**
 * This writer does not cache changes in {@link HistoryCache} and sends them to readers directly
 * (although it is used in {@link StatefullRtpsWriter}).
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
public class RtpsWriter<D extends Payload>
        extends SubmissionPublisher<RtpsMessageSender.MessageBuilder>
        implements Subscriber<D>, RtpsEntity, AutoCloseable {

    protected final XLogger logger;

    private long lastChangeNumber;
    private Guid writerGuid;
    private RtpsDataMessageBuilder lastMessage;
    private Optional<Subscription> subscriptionOpt = Optional.empty();

    private TracingToken tracingToken;

    public RtpsWriter(
            RtpsTalkConfiguration config, TracingToken tracingToken, EntityId writerEntiyId) {
        this(config, tracingToken, writerEntiyId, ReliabilityQosPolicy.Kind.BEST_EFFORT, true);
    }

    /**
     * @param pushMode Note that for a {@link ReliabilityKind#BEST_EFFORT} Writer, pushMode is true,
     *     as there are no acknowledgments. Therefore, the Writer always pushes out data as it
     *     becomes available (8.4.9.1.1)
     */
    public RtpsWriter(
            RtpsTalkConfiguration config,
            TracingToken token,
            EntityId writerEntityId,
            ReliabilityQosPolicy.Kind reliabilityKind,
            boolean pushMode) {
        this.tracingToken = new TracingToken(token, writerEntityId.toString());
        this.writerGuid = new Guid(config.guidPrefix(), writerEntityId);
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
    }

    /**
     * Internal counter used to assign increasing sequence number to each change made by the Writer.
     */
    public long getLastChangeNumber() {
        return lastChangeNumber;
    }

    public void repeatLastChange() {
        Preconditions.notNull(lastMessage);
        logger.entering("repeatLastChange");
        submit(lastMessage);
        logger.exiting("repeatLastChange");
    }

    public void newChange(D data) {
        logger.entering("newChange");
        lastChangeNumber++;
        lastMessage = new RtpsDataMessageBuilder(writerGuid.guidPrefix);
        lastMessage.add(lastChangeNumber, data);
        sendLastChangeToAllReaders();
        logger.exiting("newChange");
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
        subscriptionOpt.get().request(1);
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
        subscriptionOpt.ifPresent(Subscription::cancel);
        super.close();
        logger.fine("Closed");
    }

    public TracingToken getTracingToken() {
        return tracingToken;
    }
}
