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
package pinorobotics.rtpstalk.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.Header;
import pinorobotics.rtpstalk.impl.spec.messages.ProtocolId;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Submessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender.MessageBuilder;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/** @author lambdaprime intid@protonmail.com */
public class RtpsDataMessageBuilder implements RtpsMessageSender.MessageBuilder {

    private Map<Long, RtpsTalkMessage> data = new HashMap<>();
    private Header header;
    private Optional<GuidPrefix> readerGuidPrefix;
    private RtpsDataPackager<RtpsTalkMessage> packager = new RtpsDataPackager<>();

    public RtpsDataMessageBuilder(GuidPrefix writerGuidPrefix) {
        this(writerGuidPrefix, null);
    }

    public RtpsDataMessageBuilder(GuidPrefix writerGuidPrefix, GuidPrefix readerGuidPrefix) {
        header =
                new Header(
                        ProtocolId.Predefined.RTPS.getValue(),
                        ProtocolVersion.Predefined.Version_2_3.getValue(),
                        VendorId.Predefined.RTPSTALK.getValue(),
                        writerGuidPrefix);
        this.readerGuidPrefix = Optional.ofNullable(readerGuidPrefix);
    }

    public void add(long seqNum, RtpsTalkMessage payload) {
        data.put(seqNum, payload);
    }

    @Override
    public RtpsMessage build(EntityId readerEntiyId, EntityId writerEntityId) {
        var submessages = new ArrayList<Submessage>();
        // we do not timestamp data changes so we do not include InfoTimestamp per each Data
        // submessage,
        // instead we include InfoTimestamp per entire message and only once
        submessages.add(InfoTimestamp.now());
        for (var e : data.entrySet()) {
            var seqNum = e.getKey();
            submessages.add(
                    packager.packMessage(readerEntiyId, writerEntityId, seqNum, e.getValue()));
        }
        return new RtpsMessage(header, submessages);
    }

    public boolean hasData() {
        return !data.isEmpty();
    }

    @Override
    public GuidPrefix getReaderGuidPrefix() {
        return readerGuidPrefix.orElseGet(() -> MessageBuilder.super.getReaderGuidPrefix());
    }
}
