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
package pinorobotics.rtpstalk.impl.spec.behavior.writer;

import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Payload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;

/**
 * Stateless RTPS writer (best-effort reliability).
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class StatelessRtpsWriter<D extends Payload> extends RtpsWriter<D> {

    private DataChannelFactory channelFactory;
    private EntityId readerEntiyId;

    public StatelessRtpsWriter(
            TracingToken tracingToken,
            RtpsTalkConfiguration config,
            DataChannelFactory channelFactory,
            EntityId writerEntityId,
            EntityId readerEntiyId) {
        super(config, tracingToken, writerEntityId, ReliabilityQosPolicy.Kind.BEST_EFFORT, true);
        this.channelFactory = channelFactory;
        this.readerEntiyId = readerEntiyId;
    }

    public DataChannelFactory getChannelFactory() {
        return channelFactory;
    }

    public EntityId getReaderEntiyId() {
        return readerEntiyId;
    }
}
