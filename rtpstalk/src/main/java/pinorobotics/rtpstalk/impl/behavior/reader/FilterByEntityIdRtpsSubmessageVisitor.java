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
package pinorobotics.rtpstalk.impl.behavior.reader;

import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.AckNack;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Gap;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.walk.Result;
import pinorobotics.rtpstalk.impl.spec.messages.walk.RtpsSubmessageVisitor;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class FilterByEntityIdRtpsSubmessageVisitor implements RtpsSubmessageVisitor {

    private EntityId readerEntityId;
    private RtpsSubmessageVisitor nextVisitor;

    public FilterByEntityIdRtpsSubmessageVisitor(
            EntityId readerEntityId, RtpsSubmessageVisitor nextVisitor) {
        this.readerEntityId = readerEntityId;
        this.nextVisitor = nextVisitor;
    }

    @RtpsSpecReference(
            protocolVersion = Predefined.Version_2_3,
            paragraph = "8.3.7.2.5",
            text =
                    "The Data.readerId can be ENTITYID_UNKNOWN, in which case the Data applies to"
                            + " all Readers of that writerGUID")
    @Override
    public Result onData(GuidPrefix guidPrefix, Data data) {
        var shouldVisit = readerEntityId.equals(data.readerId);
        if (EntityId.Predefined.ENTITYID_UNKNOWN.getValue().equals(data.readerId))
            shouldVisit = true;
        return shouldVisit ? nextVisitor.onData(guidPrefix, data) : Result.CONTINUE;
    }

    @Override
    public Result onDataFrag(GuidPrefix guidPrefix, DataFrag data) {
        if (!readerEntityId.equals(data.readerId)) return Result.CONTINUE;
        return nextVisitor.onDataFrag(guidPrefix, data);
    }

    @Override
    public Result onGap(GuidPrefix guidPrefix, Gap gap) {
        if (!readerEntityId.equals(gap.readerId)) return Result.CONTINUE;
        return nextVisitor.onGap(guidPrefix, gap);
    }

    @RtpsSpecReference(
            protocolVersion = Predefined.Version_2_3,
            paragraph = "8.3.7.5.5",
            text =
                    "The Heartbeat.readerId can be ENTITYID_UNKNOWN, in which case the Heartbeat"
                            + " applies to all Readers of that writerGUID within the Participant.")
    @Override
    public Result onHeartbeat(GuidPrefix guidPrefix, Heartbeat heartbeat) {
        var shouldVisit = readerEntityId.equals(heartbeat.readerId);
        if (EntityId.Predefined.ENTITYID_UNKNOWN.getValue().equals(heartbeat.readerId))
            shouldVisit = true;
        return shouldVisit ? nextVisitor.onHeartbeat(guidPrefix, heartbeat) : Result.CONTINUE;
    }

    @Override
    public Result onAckNack(GuidPrefix guidPrefix, AckNack ackNack) {
        // AckNack submessages as readerId have remote readerId which acknowledges
        // the data and not local readerId to which submessage should be delivered
        // For that reason we does not filter AckNack submessages here
        return nextVisitor.onAckNack(guidPrefix, ackNack);
    }
}
