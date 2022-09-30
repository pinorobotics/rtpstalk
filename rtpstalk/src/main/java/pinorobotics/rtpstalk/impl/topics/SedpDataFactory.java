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

import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.qos.WriterQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.messages.DestinationOrderQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Duration;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;

/**
 * @author lambdaprime intid@protonmail.com
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SedpDataFactory {

    private RtpsTalkConfigurationInternal config;

    public SedpDataFactory(RtpsTalkConfigurationInternal config) {
        this.config = config;
    }

    public ParameterList createPublicationData(
            TopicId topicId,
            EntityId entityId,
            Locator defaultUnicastLocator,
            WriterQosPolicySet qosPolicy) {
        var guid = new Guid(config.publicConfig().guidPrefix(), entityId);
        var params = new ParameterList();
        params.put(ParameterId.PID_UNICAST_LOCATOR, defaultUnicastLocator);
        params.put(ParameterId.PID_PARTICIPANT_GUID, config.localParticipantGuid());
        params.put(ParameterId.PID_TOPIC_NAME, topicId.name());
        params.put(ParameterId.PID_TYPE_NAME, topicId.type());
        params.put(ParameterId.PID_ENDPOINT_GUID, guid);
        params.put(ParameterId.PID_KEY_HASH, guid);
        params.put(
                ParameterId.PID_PROTOCOL_VERSION,
                ProtocolVersion.Predefined.Version_2_3.getValue());
        params.put(ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue());
        params.put(
                ParameterId.PID_DURABILITY,
                new DurabilityQosPolicy(DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS));
        params.put(
                ParameterId.PID_RELIABILITY,
                new ReliabilityQosPolicy(
                        qosPolicy.reliabilityKind(), Duration.Predefined.ZERO.getValue()));
        params.put(
                ParameterId.PID_DESTINATION_ORDER,
                new DestinationOrderQosPolicy(
                        DestinationOrderQosPolicy.Kind
                                .BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS));
        return params;
    }

    public ParameterList createSubscriptionData(
            TopicId topicId,
            EntityId readerEntityId,
            Locator defaultUnicastLocator,
            ReaderQosPolicySet qosPolicy) {
        var params = new ParameterList();
        params.put(ParameterId.PID_UNICAST_LOCATOR, defaultUnicastLocator);
        params.put(ParameterId.PID_PARTICIPANT_GUID, config.localParticipantGuid());
        params.put(ParameterId.PID_TOPIC_NAME, topicId.name());
        params.put(ParameterId.PID_TYPE_NAME, topicId.type());
        params.put(
                ParameterId.PID_ENDPOINT_GUID,
                new Guid(config.publicConfig().guidPrefix(), readerEntityId));
        params.put(
                ParameterId.PID_RELIABILITY,
                new ReliabilityQosPolicy(
                        qosPolicy.reliabilityKind(), Duration.Predefined.ZERO.getValue()));
        params.put(
                ParameterId.PID_PROTOCOL_VERSION,
                ProtocolVersion.Predefined.Version_2_3.getValue());
        params.put(ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue());
        return params;
    }
}
