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
package pinorobotics.rtpstalk.impl.spec.messages;

import id.xfunction.XJsonStringBuilder;
import java.util.Objects;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DeadlineQosPolicy implements HasStreamedFields {
    public DurationT period;

    public DeadlineQosPolicy() {}

    public DeadlineQosPolicy(DurationT period) {
        this.period = period;
    }

    @Override
    public int hashCode() {
        return Objects.hash(period);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DeadlineQosPolicy other = (DeadlineQosPolicy) obj;
        return Objects.equals(period, other.period);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("period", period);
        return builder.toString();
    }
}
