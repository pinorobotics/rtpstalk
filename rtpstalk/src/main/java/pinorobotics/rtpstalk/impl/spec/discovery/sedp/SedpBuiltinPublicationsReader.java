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
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.DdsSpecReference;
import pinorobotics.rtpstalk.impl.spec.DdsVersion;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SedpBuiltinPublicationsReader
        extends StatefullReliableRtpsReader<RtpsTalkParameterListMessage> {

    @RtpsSpecReference(
            paragraph = "8.5.4.2",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "The SEDP maps the DDS built-in Entities for the DCPSSubscription,"
                            + " DCPSPublication, and DCPSTopic Topics.")
    @DdsSpecReference(paragraph = "2.2.5", protocolVersion = DdsVersion.DDS_1_4)
    private static final ReaderQosPolicySet DEFAULT_POLICY =
            new ReaderQosPolicySet(
                    ReliabilityQosPolicy.Kind.RELIABLE,
                    DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS);

    public SedpBuiltinPublicationsReader(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            LocalOperatingEntities operatingEntities) {
        super(
                config,
                tracingToken,
                RtpsTalkParameterListMessage.class,
                publisherExecutor,
                operatingEntities,
                EntityId.Predefined.ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR.getValue(),
                DEFAULT_POLICY);
    }
}
