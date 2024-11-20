/*
 * Copyright 2022 pinorobotics
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
package pinorobotics.rtpstalk.impl.topics;

import id.xfunction.util.ImmutableMultiMap;
import java.util.HashMap;
import java.util.Map;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.qos.WriterQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.DestinationOrderQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.DurationT;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;

/**
 * @author lambdaprime intid@protonmail.com
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
@RtpsSpecReference(
        protocolVersion = Predefined.Version_2_3,
        paragraph = "8.5.4.4",
        text =
                """
Data Types associated with built-in Endpoints used by the Simple Endpoint Discovery Protocol
""")
public class SedpDataFactory {

    private RtpsTalkConfigurationInternal config;

    public SedpDataFactory(RtpsTalkConfigurationInternal config) {
        this.config = config;
    }

    /** PublicationData */
    public ParameterList createDiscoveredWriterData(
            TopicId topicId,
            EntityId entityId,
            Locator defaultUnicastLocator,
            WriterQosPolicySet qosPolicy) {
        var guid = new Guid(config.publicConfig().guidPrefix(), entityId);
        return ParameterList.ofProtocolParameters(
                new ImmutableMultiMap<>(
                        Map.entry(ParameterId.PID_UNICAST_LOCATOR, defaultUnicastLocator),
                        Map.entry(ParameterId.PID_PARTICIPANT_GUID, config.localParticipantGuid()),
                        Map.entry(ParameterId.PID_TOPIC_NAME, topicId.name()),
                        Map.entry(ParameterId.PID_TYPE_NAME, topicId.type()),
                        Map.entry(ParameterId.PID_ENDPOINT_GUID, guid),
                        Map.entry(ParameterId.PID_KEY_HASH, guid),
                        Map.entry(
                                ParameterId.PID_PROTOCOL_VERSION,
                                ProtocolVersion.Predefined.Version_2_3.getValue()),
                        Map.entry(
                                ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue()),
                        Map.entry(
                                ParameterId.PID_DURABILITY,
                                new DurabilityQosPolicy(qosPolicy.durabilityKind())),
                        Map.entry(
                                ParameterId.PID_RELIABILITY,
                                new ReliabilityQosPolicy(
                                        qosPolicy.reliabilityKind(),
                                        DurationT.Predefined.ZERO.getValue())),
                        Map.entry(
                                ParameterId.PID_DESTINATION_ORDER,
                                new DestinationOrderQosPolicy(
                                        DestinationOrderQosPolicy.Kind
                                                .BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS))));
    }

    /** SubscriptionData */
    public ParameterList createDiscoveredReaderData(
            TopicId topicId,
            EntityId readerEntityId,
            Locator defaultUnicastLocator,
            ReaderQosPolicySet qosPolicy) {
        var params = new HashMap<ParameterId, Object>();
        var guid = new Guid(config.publicConfig().guidPrefix(), readerEntityId);
        return ParameterList.ofProtocolParameters(
                new ImmutableMultiMap<>(
                        Map.entry(ParameterId.PID_UNICAST_LOCATOR, defaultUnicastLocator),
                        Map.entry(ParameterId.PID_PARTICIPANT_GUID, config.localParticipantGuid()),
                        Map.entry(ParameterId.PID_TOPIC_NAME, topicId.name()),
                        Map.entry(ParameterId.PID_TYPE_NAME, topicId.type()),
                        Map.entry(ParameterId.PID_ENDPOINT_GUID, guid),
                        Map.entry(
                                ParameterId.PID_RELIABILITY,
                                new ReliabilityQosPolicy(
                                        qosPolicy.reliabilityKind(),
                                        DurationT.Predefined.ZERO.getValue())),
                        Map.entry(
                                ParameterId.PID_DURABILITY,
                                new DurabilityQosPolicy(qosPolicy.durabilityKind())),
                        Map.entry(ParameterId.PID_KEY_HASH, guid),
                        Map.entry(
                                ParameterId.PID_PROTOCOL_VERSION,
                                ProtocolVersion.Predefined.Version_2_3.getValue()),
                        Map.entry(
                                ParameterId.PID_VENDORID,
                                VendorId.Predefined.RTPSTALK.getValue())));
    }
}
