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
import java.util.Objects;

/**
 * If the RELIABILITY kind is set to RELIABLE, the write operation may block if the modification
 * would cause data to be lost or else cause one of the limits specified in the RESOURCE_LIMITS to
 * be exceeded. Under these circumstances, the RELIABILITY max_blocking_time configures the maximum
 * time the write operation may block waiting for space to become available. If max_blocking_time
 * elapses before the DataWriter is able to store the modification without exceeding the limits, the
 * write operation will fail and return TIMEOUT (2.2.2.4.2.11 write)
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ReliabilityQosPolicy {

    public enum Kind {
        UNKNOWN,
        BEST_EFFORT,
        RELIABLE;
    }

    public int kind;

    public Duration maxBlockingTime;

    public ReliabilityQosPolicy() {
        this(Kind.UNKNOWN, new Duration());
    }

    public ReliabilityQosPolicy(Kind kind, Duration maxBlockingTime) {
        this.kind = kind.ordinal();
        this.maxBlockingTime = maxBlockingTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, maxBlockingTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ReliabilityQosPolicy other = (ReliabilityQosPolicy) obj;
        return kind == other.kind && Objects.equals(maxBlockingTime, other.maxBlockingTime);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("kind", Kind.values()[kind].toString());
        builder.append("maxBlockingTime", maxBlockingTime);
        return builder.toString();
    }
}
