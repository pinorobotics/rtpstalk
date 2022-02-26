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
package pinorobotics.rtpstalk.messages;

import id.xfunction.XJsonStringBuilder;

/**
 * If the RELIABILITY kind is set to RELIABLE, the write operation may block if the modification
 * would cause data to be lost or else cause one of the limits specified in the RESOURCE_LIMITS to
 * be exceeded. Under these circumstances, the RELIABILITY max_blocking_time configures the maximum
 * time the write operation may block waiting for space to become available. If max_blocking_time
 * elapses before the DataWriter is able to store the modification without exceeding the limits, the
 * write operation will fail and return TIMEOUT (2.2.2.4.2.11 write)
 */
public class ReliabilityQosPolicy {

    public int kind;

    public Duration maxBlockingTime;

    public ReliabilityQosPolicy() {}

    public ReliabilityQosPolicy(ReliabilityKind kind, Duration maxBlockingTime) {
        this.kind = kind.getValue();
        this.maxBlockingTime = maxBlockingTime;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("kind", kind);
        builder.append("maxBlockingTime", maxBlockingTime);
        return builder.toString();
    }
}
