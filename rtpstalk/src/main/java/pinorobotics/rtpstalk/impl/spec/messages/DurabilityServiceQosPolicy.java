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
package pinorobotics.rtpstalk.impl.spec.messages;

import id.xfunction.XJsonStringBuilder;
import java.util.List;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class DurabilityServiceQosPolicy implements HasStreamedFields {
    static final List<String> STREAMED_FIELDS =
            List.of(
                    "serviceCleanupDelay",
                    "historyKind",
                    "historyDepth",
                    "maxSamples",
                    "maxInstances",
                    "maxSamplesPerInstance");
    public DurationT serviceCleanupDelay;
    public int historyKind;
    public int historyDepth;
    public int maxSamples;
    public int maxInstances;
    public int maxSamplesPerInstance;

    public DurabilityServiceQosPolicy() {}

    public DurabilityServiceQosPolicy(
            DurationT serviceCleanupDelay,
            HistoryQosPolicy.Kind historyQosPolicyKind,
            int historyDepth,
            int maxDamples,
            int maxInstances,
            int maxSamplesPerInstance) {
        this.serviceCleanupDelay = serviceCleanupDelay;
        this.historyKind = historyQosPolicyKind.ordinal();
        this.historyDepth = historyDepth;
        this.maxSamples = maxDamples;
        this.maxInstances = maxInstances;
        this.maxSamplesPerInstance = maxSamplesPerInstance;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("serviceCleanupDelay", serviceCleanupDelay);
        builder.append("historyKind", historyKind);
        builder.append("historyDepth", historyDepth);
        builder.append("maxDamples", maxSamples);
        builder.append("maxInstances", maxInstances);
        builder.append("maxSamplesPerInstance", maxSamplesPerInstance);
        return builder.toString();
    }
}
