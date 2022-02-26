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
package pinorobotics.rtpstalk.behavior.reader;

import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.walk.Result;
import pinorobotics.rtpstalk.messages.walk.RtpsSubmessageVisitor;

public class FilterByEntityIdRtpsSubmessageVisitor implements RtpsSubmessageVisitor {

    private EntityId entityId;
    private RtpsSubmessageVisitor nextVisitor;

    public FilterByEntityIdRtpsSubmessageVisitor(
            EntityId entityId, RtpsSubmessageVisitor nextVisitor) {
        this.entityId = entityId;
        this.nextVisitor = nextVisitor;
    }

    @Override
    public Result onData(GuidPrefix guidPrefix, Data data) {
        if (!entityId.equals(data.readerId)) return Result.CONTINUE;
        return nextVisitor.onData(guidPrefix, data);
    }

    @Override
    public Result onHeartbeat(GuidPrefix guidPrefix, Heartbeat heartbeat) {
        if (!entityId.equals(heartbeat.readerId)) return Result.CONTINUE;
        return nextVisitor.onHeartbeat(guidPrefix, heartbeat);
    }

    @Override
    public Result onAckNack(GuidPrefix guidPrefix, AckNack ackNack) {
        if (!entityId.equals(ackNack.readerId)) return Result.CONTINUE;
        return nextVisitor.onAckNack(guidPrefix, ackNack);
    }
}
