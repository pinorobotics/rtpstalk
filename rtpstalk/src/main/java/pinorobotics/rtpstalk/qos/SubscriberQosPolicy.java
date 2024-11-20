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
package pinorobotics.rtpstalk.qos;

import id.xfunction.XJsonStringBuilder;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public record SubscriberQosPolicy(ReliabilityType reliabilityType, DurabilityType durabilityType) {

    /**
     * Creates default Qos policy which is:
     *
     * <ul>
     *   <li>{@link ReliabilityType#RELIABLE}
     *   <li>{@link DurabilityType#TRANSIENT_LOCAL_DURABILITY_QOS}
     * </ul>
     */
    public SubscriberQosPolicy() {
        this(ReliabilityType.RELIABLE, DurabilityType.TRANSIENT_LOCAL_DURABILITY_QOS);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("reliabilityKind", reliabilityType);
        builder.append("durabilityKind", durabilityType);
        return builder.toString();
    }
}
