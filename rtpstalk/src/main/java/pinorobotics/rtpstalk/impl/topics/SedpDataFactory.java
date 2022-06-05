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
package pinorobotics.rtpstalk.impl.topics;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.impl.qos.QosPoliciesTransformer;
import pinorobotics.rtpstalk.impl.qos.SubscriberQosPolicy;
import pinorobotics.rtpstalk.messages.DestinationOrderQosPolicy;
import pinorobotics.rtpstalk.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.messages.Duration;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;

/**
 * @author lambdaprime intid@protonmail.com
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SedpDataFactory {

    private RtpsTalkConfiguration config;
    private QosPoliciesTransformer qosTransformer = new QosPoliciesTransformer();

    public SedpDataFactory(RtpsTalkConfiguration config) {
        this.config = config;
    }

    public ParameterList createPublicationData(
            TopicId topicId,
            EntityId entityId,
            Locator defaultUnicastLocator,
            PublisherQosPolicy qosPolicy) {
        var guid = new Guid(config.guidPrefix(), entityId);
        var params =
                List.<Entry<ParameterId, Object>>of(
                        Map.entry(ParameterId.PID_UNICAST_LOCATOR, defaultUnicastLocator),
                        Map.entry(
                                ParameterId.PID_PARTICIPANT_GUID, config.getLocalParticipantGuid()),
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
                                new DurabilityQosPolicy(
                                        DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS)),
                        Map.entry(
                                ParameterId.PID_RELIABILITY,
                                new ReliabilityQosPolicy(
                                        qosTransformer.toRtpsInternal(qosPolicy.reliabilityKind()),
                                        Duration.Predefined.ZERO.getValue())),
                        Map.entry(
                                ParameterId.PID_DESTINATION_ORDER,
                                new DestinationOrderQosPolicy(
                                        DestinationOrderQosPolicy.Kind
                                                .BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS)));
        return new ParameterList(params);
    }

    public ParameterList createSubscriptionData(
            TopicId topicId,
            EntityId readerEntityId,
            Locator defaultUnicastLocator,
            SubscriberQosPolicy qosPolicy) {
        var params =
                List.<Entry<ParameterId, Object>>of(
                        Map.entry(ParameterId.PID_UNICAST_LOCATOR, defaultUnicastLocator),
                        Map.entry(
                                ParameterId.PID_PARTICIPANT_GUID, config.getLocalParticipantGuid()),
                        Map.entry(ParameterId.PID_TOPIC_NAME, topicId.name()),
                        Map.entry(ParameterId.PID_TYPE_NAME, topicId.type()),
                        Map.entry(
                                ParameterId.PID_ENDPOINT_GUID,
                                new Guid(config.guidPrefix(), readerEntityId)),
                        Map.entry(
                                ParameterId.PID_RELIABILITY,
                                new ReliabilityQosPolicy(
                                        qosTransformer.toRtpsInternal(qosPolicy.reliabilityKind()),
                                        Duration.Predefined.ZERO.getValue())),
                        Map.entry(
                                ParameterId.PID_PROTOCOL_VERSION,
                                ProtocolVersion.Predefined.Version_2_3.getValue()),
                        Map.entry(
                                ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue()));
        return new ParameterList(params);
    }
}
