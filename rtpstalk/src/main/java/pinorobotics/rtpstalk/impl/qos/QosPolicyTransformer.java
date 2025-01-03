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
package pinorobotics.rtpstalk.impl.qos;

import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.qos.DurabilityType;
import pinorobotics.rtpstalk.qos.ReliabilityType;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class QosPolicyTransformer {

    public ReliabilityQosPolicy.Kind convert(ReliabilityType reliabilityType) {
        return switch (reliabilityType) {
            case BEST_EFFORT -> ReliabilityQosPolicy.Kind.BEST_EFFORT;
            case RELIABLE -> ReliabilityQosPolicy.Kind.RELIABLE;
        };
    }

    public DurabilityQosPolicy.Kind convert(DurabilityType durabilityType) {
        return switch (durabilityType) {
            case TRANSIENT_LOCAL_DURABILITY_QOS ->
                    DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS;
            case VOLATILE_DURABILITY_QOS -> DurabilityQosPolicy.Kind.VOLATILE_DURABILITY_QOS;
        };
    }
}
