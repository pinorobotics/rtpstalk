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
package pinorobotics.rtpstalk.discovery.spdp;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.BuiltinEndpointQos.EndpointQos;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet.Endpoint;
import pinorobotics.rtpstalk.messages.Duration;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;

public class SpdpDiscoveredParticipantDataFactory {

    public ParameterList createData(RtpsTalkConfiguration config) {
        var endpointSet =
                EnumSet.of(
                        Endpoint.DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR,
                        Endpoint.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_DETECTOR,
                        Endpoint.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_ANNOUNCER,
                        Endpoint.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_DETECTOR,
                        Endpoint.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_ANNOUNCER,
                        Endpoint.SECURE_PUBLICATION_READER,
                        Endpoint.PARTICIPANT_SECURE_READER,
                        Endpoint.SECURE_SUBSCRIPTION_READER,
                        Endpoint.SECURE_PARTICIPANT_MESSAGE_READER);
        // best-effort is not currently supported
        if (config.getBuiltinEndpointQos() == EndpointQos.NONE)
            endpointSet.add(Endpoint.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER);
        var params =
                List.<Entry<ParameterId, Object>>of(
                        Map.entry(
                                ParameterId.PID_PROTOCOL_VERSION,
                                ProtocolVersion.Predefined.Version_2_3.getValue()),
                        Map.entry(
                                ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue()),
                        Map.entry(
                                ParameterId.PID_PARTICIPANT_GUID,
                                new Guid(
                                        config.getGuidPrefix(),
                                        EntityId.Predefined.ENTITYID_PARTICIPANT.getValue())),
                        Map.entry(
                                ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR,
                                config.getMetatrafficUnicastLocator()),
                        Map.entry(
                                ParameterId.PID_DEFAULT_UNICAST_LOCATOR,
                                config.getDefaultUnicastLocator()),
                        Map.entry(ParameterId.PID_PARTICIPANT_LEASE_DURATION, new Duration(20)),
                        Map.entry(
                                ParameterId.PID_BUILTIN_ENDPOINT_SET,
                                new BuiltinEndpointSet(endpointSet)),
                        Map.entry(ParameterId.PID_ENTITY_NAME, "/"));
        return new ParameterList(params);
    }
}
