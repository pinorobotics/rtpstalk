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
package pinorobotics.rtpstalk.impl.topics;

import id.xfunction.XJsonStringBuilder;
import java.util.List;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public record RemoteActorDetails(
        Guid endpointGuid,
        List<Locator> writerUnicastLocator,
        ReliabilityQosPolicy.Kind reliabilityKind,
        DurabilityQosPolicy.Kind durabilityKind) {

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("endpointGuid", endpointGuid);
        builder.append("writerUnicastLocator", writerUnicastLocator);
        builder.append("reliabilityKind", reliabilityKind);
        builder.append("durabilityKind", durabilityKind);
        return builder.toString();
    }
}
