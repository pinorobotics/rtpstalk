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

import java.util.ArrayList;
import java.util.List;
import pinorobotics.rtpstalk.impl.spec.messages.Header;
import pinorobotics.rtpstalk.impl.spec.messages.ProtocolId;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Submessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsHeartbeatMessageBuilder implements RtpsMessageSender.MessageBuilder {

    private Header header;
    private long seqNumMin;
    private long seqNumMax;
    private int heartbeatCount;
    private GuidPrefix readerGuidPrefix;

    public RtpsHeartbeatMessageBuilder(
            GuidPrefix writerGuidPrefix,
            GuidPrefix readerGuidPrefix,
            long seqNumMin,
            long seqNumMax,
            int heartbeatCount) {
        this.readerGuidPrefix = readerGuidPrefix;
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
    public List<RtpsMessage> build(EntityId readerEntiyId, EntityId writerEntityId) {
        var submessages = new ArrayList<Submessage>();
        // RTPS specification does not explicitly tell all cases when INFO_DST should be included.
        // To cover situations when there are
        // multiple participants are running on same unicast locator we include it as part of
        // HEARTBEAT
        // See: https://github.com/eclipse-cyclonedds/cyclonedds/issues/1605
        submessages.add(new InfoDestination(readerGuidPrefix));
        var heartbeat =
                new Heartbeat(
                        readerEntiyId,
                        writerEntityId,
                        new SequenceNumber(seqNumMin),
                        new SequenceNumber(seqNumMax),
                        new Count(heartbeatCount));
        submessages.add(heartbeat);
        return List.of(new RtpsMessage(header, submessages.toArray(new Submessage[0])));
    }
}
