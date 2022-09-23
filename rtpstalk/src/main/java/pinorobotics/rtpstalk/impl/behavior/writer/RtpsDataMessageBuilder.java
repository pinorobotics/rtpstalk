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
package pinorobotics.rtpstalk.impl.behavior.writer;

import id.xfunction.Preconditions;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.RtpsDataPackager;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.spec.messages.Header;
import pinorobotics.rtpstalk.impl.spec.messages.ProtocolId;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Submessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender.MessageBuilder;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsDataMessageBuilder implements RtpsMessageSender.MessageBuilder {

    private Map<Long, RtpsTalkMessage> data = new LinkedHashMap<>();
    private Header header;
    private Optional<GuidPrefix> readerGuidPrefix;
    private RtpsDataPackager<RtpsTalkMessage> packager = new RtpsDataPackager<>();
    private long lastSeqNum;
    private int maxSubmessageSize;

    public RtpsDataMessageBuilder(
            RtpsTalkConfigurationInternal config, GuidPrefix writerGuidPrefix) {
        this(config, writerGuidPrefix, null);
    }

    public RtpsDataMessageBuilder(
            RtpsTalkConfigurationInternal config,
            GuidPrefix writerGuidPrefix,
            GuidPrefix readerGuidPrefix) {
        header =
                new Header(
                        ProtocolId.Predefined.RTPS.getValue(),
                        ProtocolVersion.Predefined.Version_2_3.getValue(),
                        VendorId.Predefined.RTPSTALK.getValue(),
                        writerGuidPrefix);
        this.readerGuidPrefix = Optional.ofNullable(readerGuidPrefix);
        this.maxSubmessageSize = config.maxSubmessageSize();
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

    public boolean hasData() {
        return !data.isEmpty();
    }

    @Override
    public List<RtpsMessage> build(EntityId readerEntiyId, EntityId writerEntityId) {
        var messages = new ArrayList<RtpsMessage>();
        var messageBuilder = new InternalBuilder();
        for (var e : data.entrySet()) {
            var seqNum = e.getKey();
            var message = e.getValue();
            if (!needsFragmentation(message)) {
                var submessage =
                        packager.packMessage(readerEntiyId, writerEntityId, seqNum, message);
                if (messageBuilder.add(submessage)) continue;
                // the message builder is already full so we reset it
                messageBuilder.build().ifPresent(messages::add);
                messageBuilder = new InternalBuilder();
                if (messageBuilder.add(submessage)) continue;
                // we could not added the submessage into empty message
                // trying to use fragmentation
            }
            if (message instanceof RtpsTalkDataMessage dataMessage) {
                // reset submessages in the builder if any
                messageBuilder.build().ifPresent(messages::add);
                messageBuilder = new InternalBuilder();
                var inlineQos =
                        message.userInlineQos().map(v -> new ParameterList(v.getParameters()));
                for (var fragment :
                        new DataFragmentSplitter(
                                readerEntiyId,
                                writerEntityId,
                                seqNum,
                                inlineQos,
                                dataMessage.data().get(),
                                maxSubmessageSize - messageBuilder.getSize())) {
                    Preconditions.isTrue(
                            messageBuilder.add(fragment),
                            "DataFrag submessage cannot be added to RTPS message");
                    messages.add(messageBuilder.build().get());
                    messageBuilder = new InternalBuilder();
                }
            } else {
                throw new UnsupportedOperationException(
                        "Fragmentation of " + message.getClass().getSimpleName());
            }
        }
        messageBuilder.build().ifPresent(messages::add);
        return messages;
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
    private class InternalBuilder {
        static final int headerLen = LengthCalculator.getInstance().getFixedLength(Header.class);
        List<Submessage> submessages = new ArrayList<Submessage>();
        int messageLen = headerLen;

        InternalBuilder() {
            // we do not timestamp data changes so we do not include InfoTimestamp per each Data
            // submessage, instead we include InfoTimestamp per entire message and only once
            submessages.add(InfoTimestamp.now());
            messageLen += LengthCalculator.getInstance().getFixedLength(InfoTimestamp.class);
        }

        public int getSize() {
            return messageLen;
        }

        boolean add(Submessage submessage) {
            var submessageLen = submessage.submessageHeader.submessageLength.getUnsigned();
            if (messageLen + submessageLen > maxSubmessageSize) return false;
            submessages.add(submessage);
            messageLen += submessageLen;
            return true;
        }

        public Optional<RtpsMessage> build() {
            return submessages.isEmpty()
                    ? Optional.empty()
                    : Optional.of(new RtpsMessage(header, submessages));
        }
    }
}