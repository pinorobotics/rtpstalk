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
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;

/** @author lambdaprime intid@protonmail.com */
public class RtpsDataMessageBuilder implements RtpsMessageBuilder {

    private Map<Long, Payload> data = new HashMap<>();
    private Header header;

    public RtpsDataMessageBuilder(GuidPrefix writerGuidPrefix) {
        header =
                new Header(
                        ProtocolId.Predefined.RTPS.getValue(),
                        ProtocolVersion.Predefined.Version_2_3.getValue(),
                        VendorId.Predefined.RTPSTALK.getValue(),
                        writerGuidPrefix);
    }

    public void add(long seqNum, Payload payload) {
        data.put(seqNum, payload);
    }

    @Override
    public RtpsMessage build(EntityId readerEntiyId, EntityId writerEntityId) {
        var submessages = new ArrayList<Submessage>();
        submessages.add(InfoTimestamp.now());
        data.entrySet().stream()
                .map(
                        entry ->
                                new Data(
                                        readerEntiyId,
                                        writerEntityId,
                                        new SequenceNumber(entry.getKey()),
                                        new SerializedPayload(entry.getValue())))
                .forEach(submessages::add);
        return new RtpsMessage(header, submessages);
    }

    public boolean hasData() {
        return !data.isEmpty();
    }
}
