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
package pinorobotics.rtpstalk.impl.spec.messages.walk;

import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.AckNack;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoTimestamp;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsSubmessagesWalker {

    public void walk(RtpsMessage message, RtpsSubmessageVisitor visitor) {
        var guidPrefix = message.header.guidPrefix;
        for (var submessage : message.getSubmessages()) {
            Result res = null;
            if (submessage instanceof Data data) res = visitor.onData(guidPrefix, data);
            else if (submessage instanceof DataFrag dataFrag)
                res = visitor.onDataFrag(guidPrefix, dataFrag);
            else if (submessage instanceof AckNack ackNack)
                res = visitor.onAckNack(guidPrefix, ackNack);
            else if (submessage instanceof Heartbeat heartbeat)
                res = visitor.onHeartbeat(guidPrefix, heartbeat);
            else if (submessage instanceof InfoTimestamp infoTimestamp)
                res = visitor.onInfoTimestamp(guidPrefix, infoTimestamp);
            else if (submessage instanceof InfoDestination infoDestination)
                res = visitor.onInfoDestination(guidPrefix, infoDestination);
            else res = Result.CONTINUE;
            if (res == Result.STOP) break;
        }
    }
}
