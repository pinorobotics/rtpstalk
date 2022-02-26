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
package pinorobotics.rtpstalk.messages.walk;

import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class RtpsSubmessagesWalker {

    public void walk(RtpsMessage message, RtpsSubmessageVisitor visitor) {
        var guidPrefix = message.header.guidPrefix;
        for (var submessage : message.getSubmessages()) {
            var res =
                    switch (submessage) {
                        case Data data -> visitor.onData(guidPrefix, data);
                        case AckNack ackNack -> visitor.onAckNack(guidPrefix, ackNack);
                        case Heartbeat heartbeat -> visitor.onHeartbeat(guidPrefix, heartbeat);
                        case InfoTimestamp infoTimestamp -> visitor.onInfoTimestamp(
                                guidPrefix, infoTimestamp);
                        case InfoDestination infoDestination -> visitor.onInfoDestination(
                                guidPrefix, infoDestination);
                        default -> Result.CONTINUE;
                    };
            if (res == Result.STOP) break;
        }
    }
}
