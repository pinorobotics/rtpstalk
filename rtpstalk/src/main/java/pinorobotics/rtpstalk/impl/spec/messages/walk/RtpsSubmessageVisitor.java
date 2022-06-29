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

import pinorobotics.rtpstalk.impl.spec.messages.submessages.AckNack;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;

/**
 * Visitor of submessages inside of RTPS message.
 *
 * <p>Each method accepts guidPrefix which is taken from the header of the RTPS message itself.
 */
/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public interface RtpsSubmessageVisitor {

    default Result onAckNack(GuidPrefix guidPrefix, AckNack ackNack) {
        return Result.CONTINUE;
    }

    default Result onData(GuidPrefix guidPrefix, Data data) {
        return Result.CONTINUE;
    }

    default Result onHeartbeat(GuidPrefix guidPrefix, Heartbeat heartbeat) {
        return Result.CONTINUE;
    }

    default Result onInfoTimestamp(GuidPrefix guidPrefix, InfoTimestamp infoTimestamp) {
        return Result.CONTINUE;
    }

    default Result onInfoDestination(GuidPrefix guidPrefix, InfoDestination infoDestination) {
        return Result.CONTINUE;
    }
}
