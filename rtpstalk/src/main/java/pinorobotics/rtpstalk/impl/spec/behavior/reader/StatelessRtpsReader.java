/*
 * Copyright 2024 pinorobotics
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
package pinorobotics.rtpstalk.impl.spec.behavior.reader;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import java.util.concurrent.Executor;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiver;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * Stateless RTPS endpoint reader (best-effort reliability) which can be subscribed to {@link
 * RtpsMessageReceiver} to receive RTPS messages and process them.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class StatelessRtpsReader<D extends RtpsTalkMessage> extends RtpsReader<D> {

    public StatelessRtpsReader(
            RtpsTalkConfiguration config,
            TracingToken token,
            Class<D> messageType,
            Executor publisherExecutor,
            EntityId entityId,
            ReliabilityQosPolicy.Kind reliabilityKind) {
        super(
                config,
                token,
                messageType,
                publisherExecutor,
                new Guid(config.guidPrefix(), entityId),
                reliabilityKind);
        Preconditions.equals(reliabilityKind, ReliabilityQosPolicy.Kind.BEST_EFFORT);
    }
}
