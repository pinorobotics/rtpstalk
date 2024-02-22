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
package pinorobotics.rtpstalk.impl.spec.discovery.spdp;

import id.xfunction.util.ImmutableMultiMap;
import java.util.EnumSet;
import pinorobotics.rtpstalk.EndpointQos;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet.Endpoint;
import pinorobotics.rtpstalk.impl.spec.messages.Duration;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.UnsignedInt;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
@RtpsSpecReference(
        protocolVersion = Predefined.Version_2_3,
        paragraph = "9.6.2.2",
        text =
                """
            The discovery data is sent using standard Data Submessages. In order to allow for QoS extensibility while preserving
            interoperability between versions of the protocol, the wire-representation of the SerializedData within the Data
            Submessage uses a the format of a ParameterList SubmessageElement.
            """)
public class SpdpDiscoveredParticipantDataFactory {

    public ParameterList createData(
            RtpsTalkConfigurationInternal config,
            Locator metatrafficUnicastLocator,
            Locator defaultUnicastLocator) {
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
        if (config.publicConfig().builtinEndpointQos() == EndpointQos.NONE)
            endpointSet.add(Endpoint.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER);
        var params =
                ParameterList.ofProtocolParameters(
                        ImmutableMultiMap.of(
                                ParameterId.PID_PROTOCOL_VERSION,
                                ProtocolVersion.Predefined.Version_2_3.getValue(),
                                ParameterId.PID_VENDORID,
                                VendorId.Predefined.RTPSTALK.getValue(),
                                ParameterId.PID_PARTICIPANT_GUID,
                                config.localParticipantGuid(),
                                ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR,
                                metatrafficUnicastLocator,
                                ParameterId.PID_DEFAULT_UNICAST_LOCATOR,
                                defaultUnicastLocator,
                                ParameterId.PID_PARTICIPANT_LEASE_DURATION,
                                new Duration(20),
                                ParameterId.PID_DOMAIN_ID,
                                new UnsignedInt(config.publicConfig().domainId()),
                                ParameterId.PID_BUILTIN_ENDPOINT_SET,
                                new BuiltinEndpointSet(endpointSet),
                                ParameterId.PID_ENTITY_NAME,
                                "/"));
        return params;
    }
}
