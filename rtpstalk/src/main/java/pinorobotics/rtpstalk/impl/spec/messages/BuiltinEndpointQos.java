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

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import pinorobotics.rtpstalk.EndpointQos;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class BuiltinEndpointQos {

    public int bitmask;

    public BuiltinEndpointQos() {}

    public BuiltinEndpointQos(EnumSet<EndpointQos> set) {
        var bset = new BitSet();
        set.stream()
                .filter(Predicate.isEqual(EndpointQos.NONE).negate())
                .forEach(p -> bset.set(p.getValue()));
        bitmask = (int) bset.toLongArray()[0];
    }

    public boolean hasFlag(EndpointQos flag) {
        return BitSet.valueOf(new long[] {bitmask}).get(flag.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(bitmask);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BuiltinEndpointQos other = (BuiltinEndpointQos) obj;
        return bitmask == other.bitmask;
    }

    @Override
    public String toString() {
        var set = BitSet.valueOf(new long[] {bitmask});
        var str =
                set.stream()
                        .mapToObj(pos -> EndpointQos.valueOf(pos))
                        .map(EndpointQos::name)
                        .collect(Collectors.joining(" | "));
        return str;
    }
}
