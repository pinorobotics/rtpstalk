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
package pinorobotics.rtpstalk.impl.spec.behavior.liveliness;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointQos.EndpointQos;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;

/**
 * Reliable liveliness reader
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class BuiltinParticipantMessageReader
        extends StatefullReliableRtpsReader<RtpsTalkParameterListMessage> {

    public BuiltinParticipantMessageReader(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            OperatingEntities operatingEntities) {
        super(
                config,
                tracingToken,
                operatingEntities,
                EntityId.Predefined.ENTITYID_P2P_BUILTIN_PARTICIPANT_MESSAGE_READER.getValue());
        Preconditions.isTrue(
                config.builtinEndpointQos()
                        != EndpointQos.BEST_EFFORT_PARTICIPANT_MESSAGE_DATA_READER,
                "Not supported with best effort builtin endpoint Qos");
    }
}
