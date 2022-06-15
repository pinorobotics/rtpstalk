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
package pinorobotics.rtpstalk.impl.spec.userdata;

import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class DataObjectsFactory {

    public DataReader newDataReader(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            OperatingEntities operatingEntities,
            EntityId eid) {
        return new DataReader(config, tracingToken, operatingEntities, eid);
    }

    public DataWriter newDataWriter(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            DataChannelFactory channelFactory,
            OperatingEntities operatingEntities,
            EntityId writerEntityId) {
        return new DataWriter(
                config, tracingToken, channelFactory, operatingEntities, writerEntityId);
    }
}