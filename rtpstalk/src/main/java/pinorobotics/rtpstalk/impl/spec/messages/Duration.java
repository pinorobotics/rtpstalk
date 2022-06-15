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
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class Duration {

    public static enum Predefined {
        ZERO(new Duration(0, 0)),
        INFINITE(new Duration(0x7fffffff, 0xffffffff));

        static final Map<Duration, Predefined> MAP =
                Arrays.stream(Predefined.values()).collect(Collectors.toMap(k -> k.value, v -> v));
        private Duration value;

        Predefined(Duration value) {
            this.value = value;
        }

        public Duration getValue() {
            return value;
        }
    }

    public int seconds;

    /** Time in sec/2^32 */
    public int fraction;

    public Duration() {}

    public Duration(int seconds, int fraction) {
        this.seconds = seconds;
        this.fraction = fraction;
    }

    public Duration(int seconds) {
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
        Duration other = (Duration) obj;
        return fraction == other.fraction && seconds == other.seconds;
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
}
