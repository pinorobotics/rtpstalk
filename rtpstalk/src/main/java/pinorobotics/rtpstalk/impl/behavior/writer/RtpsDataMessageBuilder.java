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
package pinorobotics.rtpstalk.impl.behavior.writer;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.RtpsDataPackager;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.messages.RtpsMessageAggregator;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Gap;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Submessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender.MessageBuilder;
import pinorobotics.rtpstalk.impl.spec.userdata.SampleIdentityProcessor;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * Build sequence of {@link RtpsMessage}, which is ready to be send, from different kinds of {@link
 * RtpsTalkMessage}
 *
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsDataMessageBuilder implements RtpsMessageSender.MessageBuilder {

    private XLogger logger;
    private Map<Long, RtpsTalkMessage> data = new LinkedHashMap<>();
    private GuidPrefix writerGuidPrefix;
    private Optional<GuidPrefix> readerGuidPrefix;
    private RtpsDataPackager<RtpsTalkMessage> packager = new RtpsDataPackager<>();
    private SampleIdentityProcessor identityProc = new SampleIdentityProcessor();
    private long lastSeqNum;
    private int maxSubmessageSize;
    private TracingToken tracingToken;

    public RtpsDataMessageBuilder(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            GuidPrefix writerGuidPrefix) {
        this(config, tracingToken, writerGuidPrefix, null);
    }

    public RtpsDataMessageBuilder(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            GuidPrefix writerGuidPrefix,
            GuidPrefix readerGuidPrefix) {
        this.tracingToken = tracingToken;
        this.writerGuidPrefix = writerGuidPrefix;
        this.readerGuidPrefix = Optional.ofNullable(readerGuidPrefix);
        this.maxSubmessageSize = config.maxSubmessageSize();
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    public RtpsDataMessageBuilder add(long seqNum, RtpsTalkMessage payload) {
        Preconditions.isLess(lastSeqNum, seqNum, "Change is out of order");
        data.put(seqNum, payload);
        lastSeqNum = seqNum;
        return this;
    }

    public RtpsDataMessageBuilder addAll(RtpsDataMessageBuilder other) {
        data.putAll(other.data);
        return this;
    }

    @Override
    public List<RtpsMessage> build(EntityId readerEntiyId, EntityId writerEntityId) {
        var messages = new ArrayList<RtpsMessage>();
        var messageBuilder = new InternalBuilder(writerGuidPrefix);
        for (var e : data.entrySet()) {
            var seqNum = e.getKey();
            var message = e.getValue();
            if (!needsFragmentation(message)) {
                var submessage = createSubmessage(readerEntiyId, writerEntityId, seqNum, message);
                if (messageBuilder.add(submessage)) continue;
                // the message builder is already full so we reset it
                messageBuilder.build().ifPresent(messages::add);
                messageBuilder = new InternalBuilder(writerGuidPrefix);
                if (messageBuilder.add(submessage)) continue;
                // we could not add the submessage into empty message
                // trying to use fragmentation
            }
            if (message instanceof RtpsTalkDataMessage dataMessage) {
                // reset submessages in the builder if any
                messageBuilder.build().ifPresent(messages::add);
                messageBuilder = new InternalBuilder(writerGuidPrefix);
                logger.fine(
                        "Data from message with sequence number {0} does not fit into RTPS Data"
                                + " message and will be fragmented",
                        seqNum);
                var inlineQos =
                        message.userInlineQos()
                                .map(
                                        v ->
                                                ParameterList.ofUserParameters(
                                                        v.getParameters().entrySet()));
                var fragmentSize = maxSubmessageSize - messageBuilder.getSize();
                if (readerGuidPrefix.isEmpty()) {
                    // each time we resend fragmented message it should always have fragmentSize
                    // which was specified in the original fragmented message
                    // because original and resent fragmented messages may or may not contain
                    // InfoDestination
                    // submessage (depending on presence of readerGuidPrefix) we account
                    // fragmentSize for both the cases
                    fragmentSize -= InfoDestination.SIZE;
                }
                for (var fragment :
                        new DataFragmentSplitter(
                                tracingToken,
                                readerEntiyId,
                                writerEntityId,
                                seqNum,
                                inlineQos,
                                dataMessage.data().get(),
                                fragmentSize)) {
                    Preconditions.isTrue(
                            messageBuilder.add(fragment),
                            "DataFrag submessage cannot be added to RTPS message");
                    messages.add(messageBuilder.build().get());
                    messageBuilder = new InternalBuilder(writerGuidPrefix);
                }
            } else {
                throw new UnsupportedOperationException(
                        "Fragmentation of " + message.getClass().getSimpleName());
            }
        }
        messageBuilder.build().ifPresent(messages::add);
        return messages;
    }

    private Submessage createSubmessage(
            EntityId readerEntiyId, EntityId writerEntityId, Long seqNum, RtpsTalkMessage message) {
        if (message instanceof RtpsTalkDataMessage data) {
            if (readerGuidPrefix
                    .map(
                            prefix ->
                                    identityProc.shouldReplaceWithGap(
                                            data, prefix, writerGuidPrefix))
                    .orElse(false)) {
                return new Gap(
                        readerEntiyId,
                        writerEntityId,
                        new SequenceNumber(seqNum),
                        new SequenceNumberSet(seqNum + 1));
            }
        }
        return packager.packMessage(readerEntiyId, writerEntityId, seqNum, message);
    }

    private boolean needsFragmentation(RtpsTalkMessage message) {
        if (message instanceof RtpsTalkDataMessage dataMessage) {
            return message.userInlineQos().map(this::calculateSize).orElse(0)
                            + dataMessage.data().map(a -> a.length).orElse(0)
                    > maxSubmessageSize;
        }
        return false;
    }

    private int calculateSize(Parameters parameters) {
        var params = parameters.getParameters();
        return params.keySet().size() * Short.BYTES
                + params.values().stream().mapToInt(a -> a.length).sum();
    }

    @Override
    public GuidPrefix getReaderGuidPrefix() {
        return readerGuidPrefix.orElseGet(() -> MessageBuilder.super.getReaderGuidPrefix());
    }

    /** Aggregates submessages into single message until it becomes full */
    private class InternalBuilder extends RtpsMessageAggregator {

        InternalBuilder(GuidPrefix writerGuidPrefix) {
            super(tracingToken, writerGuidPrefix, maxSubmessageSize);
            // RTPS specification does not explicitly tell all the cases when INFO_DST should be
            // included.
            // To cover situations when there are
            // multiple participants running on same unicast locator we include it as part of
            // DATA
            // See: https://github.com/eclipse-cyclonedds/cyclonedds/issues/1605
            readerGuidPrefix.map(InfoDestination::new).ifPresent(this::add);
            // we do not timestamp data changes so we do not include InfoTimestamp per each Data
            // submessage, instead we include InfoTimestamp per entire RtpsMessage message and only
            // once
            add(InfoTimestamp.now());
        }
    }

    public int getDataCount() {
        return data.size();
    }
}
