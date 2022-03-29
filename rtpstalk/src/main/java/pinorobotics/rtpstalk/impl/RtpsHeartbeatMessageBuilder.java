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

import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;

/** @author lambdaprime intid@protonmail.com */
public class RtpsHeartbeatMessageBuilder implements RtpsMessageBuilder {

    private Header header;
    private long seqNumMin;
    private long seqNumMax;
    private int heartbeatCount;

    public RtpsHeartbeatMessageBuilder(
            GuidPrefix writerGuidPrefix, long seqNumMin, long seqNumMax, int heartbeatCount) {
        this.seqNumMin = seqNumMin;
        this.seqNumMax = seqNumMax;
        this.heartbeatCount = heartbeatCount;
        this.header =
                new Header(
                        ProtocolId.Predefined.RTPS.getValue(),
                        ProtocolVersion.Predefined.Version_2_3.getValue(),
                        VendorId.Predefined.RTPSTALK.getValue(),
                        writerGuidPrefix);
    }

    @Override
    public RtpsMessage build(EntityId readerEntiyId, EntityId writerEntityId) {
        var heartbeat =
                new Heartbeat(
                        readerEntiyId,
                        writerEntityId,
                        new SequenceNumber(seqNumMin),
                        new SequenceNumber(seqNumMax),
                        new Count(heartbeatCount));
        var submessages = new Submessage[] {heartbeat};
        return new RtpsMessage(header, submessages);
    }
}
