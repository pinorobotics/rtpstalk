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

import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import java.util.Optional;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.ReliabilityKind;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.structure.HistoryCache;
import pinorobotics.rtpstalk.structure.RtpsEntity;

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
 */
public class RtpsWriter<D extends Payload> extends SubmissionPublisher<RtpsMessage>
        implements Processor<D, RtpsMessage>, RtpsEntity, AutoCloseable {

    protected final XLogger logger;

    private long lastChangeNumber;
    private Guid writerGuid;
    private EntityId readerEntiyId;
    private RtpsMessage lastMessage;
    private Optional<Subscription> subscriptionOpt = Optional.empty();

    public RtpsWriter(
            RtpsTalkConfiguration config, EntityId writerEntiyId, EntityId readerEntiyId) {
        this(config, writerEntiyId, readerEntiyId, ReliabilityKind.BEST_EFFORT, true);
    }

    /**
     * @param pushMode Note that for a {@link ReliabilityKind#BEST_EFFORT} Writer, pushMode is true,
     *     as there are no acknowledgments. Therefore, the Writer always pushes out data as it
     *     becomes available (8.4.9.1.1)
     */
    public RtpsWriter(
            RtpsTalkConfiguration config,
            EntityId writerEntityId,
            EntityId readerEntiyId,
            ReliabilityKind reliabilityKind,
            boolean pushMode) {
        this.writerGuid = new Guid(config.getGuidPrefix(), writerEntityId);
        this.readerEntiyId = readerEntiyId;
        logger = InternalUtils.getInstance().getLogger(getClass(), writerGuid.entityId);
    }

    /**
     * Internal counter used to assign increasing sequence number to each change made by the Writer.
     */
    public long getLastChangeNumber() {
        return lastChangeNumber;
    }

    public void repeatLastChange() {
        XAsserts.assertNotNull(lastMessage);
        logger.entering("repeatLastChange");
        submit(lastMessage);
        logger.exiting("repeatLastChange");
    }

    public void newChange(D data) {
        logger.entering("newChange");
        lastChangeNumber++;
        var dataSubmessage =
                new Data(
                        readerEntiyId,
                        writerGuid.entityId,
                        new SequenceNumber(lastChangeNumber),
                        new SerializedPayload(data));
        var submessages = new Submessage[] {InfoTimestamp.now(), dataSubmessage};
        var header =
                new Header(
                        ProtocolId.Predefined.RTPS.getValue(),
                        ProtocolVersion.Predefined.Version_2_3.getValue(),
                        VendorId.Predefined.RTPSTALK.getValue(),
                        writerGuid.guidPrefix);
        lastMessage = new RtpsMessage(header, submessages);
        submit(lastMessage);
        logger.exiting("newChange");
    }

    @Override
    public Guid getGuid() {
        return writerGuid;
    }

    public EntityId getReaderEntiyId() {
        return readerEntiyId;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        XAsserts.assertTrue(this.subscriptionOpt.isEmpty(), "Already subscribed");
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
        super.close();
        subscriptionOpt.ifPresent(Subscription::cancel);
    }
}
