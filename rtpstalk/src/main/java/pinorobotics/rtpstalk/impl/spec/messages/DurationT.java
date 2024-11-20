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
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DurationT implements HasStreamedFields {
    static final List<String> STREAMED_FIELDS = List.of("seconds", "fraction");

    public static enum Predefined {
        ZERO(new DurationT(0, 0)),
        INFINITE(new DurationT(0x7fffffff, 0xffffffffL));

        static final Map<DurationT, Predefined> MAP =
                Arrays.stream(Predefined.values()).collect(Collectors.toMap(k -> k.value, v -> v));
        private DurationT value;

        Predefined(DurationT value) {
            this.value = value;
        }

        public DurationT getValue() {
            return value;
        }
    }

    public UnsignedInt seconds;

    /** Time in sec/2^32 */
    public UnsignedInt fraction;

    public DurationT() {}

    public DurationT(long seconds, long fraction) {
        this.seconds = new UnsignedInt(seconds);
        this.fraction = new UnsignedInt(fraction);
    }

    public DurationT(long seconds) {
        this(seconds, 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fraction, seconds);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DurationT other = (DurationT) obj;
        return Objects.equals(fraction, other.fraction) && seconds == other.seconds;
    }

    @Override
    public String toString() {
        var predefined = Predefined.MAP.get(this);
        if (predefined != null) {
            return predefined.name();
        }
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("seconds", seconds);
        builder.append("fraction", fraction);
        return builder.toString();
    }

    public Duration toDuration() {
        return Duration.ofSeconds(seconds.getUnsigned());
    }
}
