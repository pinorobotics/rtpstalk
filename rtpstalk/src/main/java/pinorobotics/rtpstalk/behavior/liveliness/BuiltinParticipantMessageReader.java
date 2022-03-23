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
package pinorobotics.rtpstalk.behavior.liveliness;

import id.xfunction.XAsserts;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.messages.BuiltinEndpointQos.EndpointQos;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;

/** Reliable liveliness reader */
/** @author aeon_flux aeon_flux@eclipso.ch */
public class BuiltinParticipantMessageReader extends StatefullRtpsReader<ParameterList> {

    public BuiltinParticipantMessageReader(RtpsTalkConfiguration config) {
        super(
                config,
                EntityId.Predefined.ENTITYID_P2P_BUILTIN_PARTICIPANT_MESSAGE_READER.getValue());
        XAsserts.assertTrue(
                config.builtinEndpointQos()
                        != EndpointQos.BEST_EFFORT_PARTICIPANT_MESSAGE_DATA_READER,
                "Not supported with best effort builtin endpoint Qos");
    }
}
