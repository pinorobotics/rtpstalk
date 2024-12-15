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
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatelessRtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class BestEffortDataReader extends StatelessRtpsReader<RtpsTalkDataMessage> {
    private static final SampleIdentityProcessor IDENTITY_PROC = new SampleIdentityProcessor();

    protected BestEffortDataReader(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            EntityId entityId,
            ReaderQosPolicySet readerQosPolicy) {
        super(
                config.publicConfig(),
                tracingToken,
                RtpsTalkDataMessage.class,
                publisherExecutor,
                entityId,
                readerQosPolicy.reliabilityKind());
    }

    @Override
    protected void processInlineQos(
            Guid writer,
            SequenceNumber seqNum,
            RtpsTalkDataMessage message,
            ParameterList inlineQos) {
        super.processInlineQos(writer, seqNum, message, inlineQos);
        message.userInlineQos()
                .ifPresent(params -> IDENTITY_PROC.updateSampleIdentity(params, seqNum));
    }
}
