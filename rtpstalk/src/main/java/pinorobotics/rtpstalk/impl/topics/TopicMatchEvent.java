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

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.impl.TopicId;

/**
 * This event is triggered when there is a match between {@link TopicId} one of:
 *
 * <ul>
 *   <li>available remote topic publishers in the network and
 *   <li>user topic subscriber
 * </ul>
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public record TopicMatchEvent<A>(RemoteActorDetails remoteActor, A localActor) {

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("remoteActor", remoteActor);
        builder.append("localActor", localActor);
        return builder.toString();
    }
}
