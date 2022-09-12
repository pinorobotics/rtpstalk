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
package pinorobotics.rtpstalk.impl.spec.discovery.sedp;

import id.xfunction.logging.TracingToken;
import java.util.concurrent.Executor;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.qos.PublisherQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SedpBuiltinSubscriptionsWriter
        extends StatefullReliableRtpsWriter<RtpsTalkParameterListMessage> {
    private static final PublisherQosPolicySet DEFAULT_POLICY =
            new PublisherQosPolicySet(
                    ReliabilityQosPolicy.Kind.RELIABLE,
                    DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS);

    public SedpBuiltinSubscriptionsWriter(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            DataChannelFactory channelFactory,
            OperatingEntities operatingEntities) {
        super(
                config,
                tracingToken,
                publisherExecutor,
                channelFactory,
                operatingEntities,
                EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER.getValue(),
                DEFAULT_POLICY);
    }
}
