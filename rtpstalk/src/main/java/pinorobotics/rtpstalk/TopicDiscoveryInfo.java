/*
 * Copyright 2026 pinorobotics
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
package pinorobotics.rtpstalk;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.qos.DurabilityType;
import pinorobotics.rtpstalk.qos.ReliabilityType;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public record TopicDiscoveryInfo(
        String topicName,
        String topicType,
        boolean isPublisher,
        byte[] endpointGuid,
        ReliabilityType reliabilityType,
        DurabilityType durabilityType) {
    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("topicName", topicName);
        builder.append("topicType", topicType);
        builder.append("isPublisher", isPublisher);
        builder.append("endpointGuid", endpointGuid);
        builder.append("reliabilityType", reliabilityType);
        builder.append("durabilityType", durabilityType);
        return builder.toString();
    }
}
