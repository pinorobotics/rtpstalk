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

import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BuiltinEndpointQos {

    public static enum EndpointQos {
        BEST_EFFORT_PARTICIPANT_MESSAGE_DATA_READER(0),
        NONE(-1);

        static final Map<Integer, EndpointQos> MAP =
                Arrays.stream(EndpointQos.values())
                        .collect(Collectors.toMap(k -> k.position, v -> v));
        private int position;

        EndpointQos(int position) {
            this.position = position;
        }
    }

    public int value;

    public BuiltinEndpointQos() {}

    public BuiltinEndpointQos(EnumSet<EndpointQos> set) {
        var bset = new BitSet();
        set.stream()
                .filter(Predicate.isEqual(EndpointQos.NONE).negate())
                .forEach(p -> bset.set(p.position));
        value = (int) bset.toLongArray()[0];
    }

    public boolean hasFlag(EndpointQos flag) {
        return BitSet.valueOf(new long[] {value}).get(flag.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BuiltinEndpointQos other = (BuiltinEndpointQos) obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        var set = BitSet.valueOf(new long[] {value});
        var str =
                set.stream()
                        .mapToObj(pos -> EndpointQos.MAP.getOrDefault(pos, EndpointQos.NONE))
                        .map(EndpointQos::name)
                        .collect(Collectors.joining(" | "));
        return str;
    }
}
