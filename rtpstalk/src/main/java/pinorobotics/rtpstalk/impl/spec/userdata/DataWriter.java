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
package pinorobotics.rtpstalk.impl.spec.userdata;

import id.xfunction.logging.TracingToken;
import java.util.concurrent.Executor;
import pinorobotics.rtpstalk.WriterSettings;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.qos.WriterQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataWriter extends StatefullReliableRtpsWriter<RtpsTalkDataMessage> {
    public DataWriter(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            DataChannelFactory channelFactory,
            LocalOperatingEntities operatingEntities,
            EntityId writerEntityId,
            WriterQosPolicySet qosPolicy,
            WriterSettings writerSettings) {
        super(
                config,
                tracingToken,
                publisherExecutor,
                channelFactory,
                operatingEntities,
                writerEntityId,
                qosPolicy,
                writerSettings);
    }
}
