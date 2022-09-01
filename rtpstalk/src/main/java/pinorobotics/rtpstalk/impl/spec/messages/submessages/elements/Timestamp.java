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
package pinorobotics.rtpstalk.impl.spec.messages.submessages.elements;

import id.xfunction.XJsonStringBuilder;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import pinorobotics.rtpstalk.impl.spec.messages.UnsignedInt;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class Timestamp implements SubmessageElement {

    public static enum Predefined {
        TIME_ZERO(new Timestamp(0, 0)),
        TIME_INVALID(new Timestamp(0xffffffff, 0xffffffffL)),
        TIME_INFINITE(new Timestamp(0xffffffff, 0xfffffffeL));

        static final Map<Timestamp, Predefined> MAP =
                Arrays.stream(Predefined.values()).collect(Collectors.toMap(k -> k.value, v -> v));
        private Timestamp value;

        Predefined(Timestamp value) {
            this.value = value;
        }

        public Timestamp getValue() {
            return value;
        }
    }

    public int seconds;

    /** Time in sec/2^32 */
    public UnsignedInt fraction;

    public Timestamp() {
        // TODO Auto-generated constructor stub
    }

    public Timestamp(long seconds, long fraction) {
        this.seconds = (int) seconds;
        this.fraction = new UnsignedInt(fraction);
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
        Timestamp other = (Timestamp) obj;
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

    public static Timestamp now() {
        var secs = Instant.now().getEpochSecond();
        var fraction = secs / (1 << 31);
        return new Timestamp(secs, fraction);
    }
}
