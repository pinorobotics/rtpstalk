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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class ProtocolVersion {

    public static enum Predefined {
        Version_2_2(new ProtocolVersion(2, 2)),
        Version_2_3(new ProtocolVersion(2, 3));

        static final EnumSet<Predefined> SUPPORTED =
                EnumSet.of(Predefined.Version_2_2, Predefined.Version_2_3);
        static final Map<ProtocolVersion, Predefined> MAP =
                Arrays.stream(Predefined.values()).collect(Collectors.toMap(k -> k.value, v -> v));
        private ProtocolVersion value;

        Predefined(ProtocolVersion value) {
            this.value = value;
        }

        public ProtocolVersion getValue() {
            return value;
        }
    }

    public byte major;

    public byte minor;

    public ProtocolVersion() {}

    public ProtocolVersion(int major, int minor) {
        this.major = (byte) major;
        this.minor = (byte) minor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ProtocolVersion other = (ProtocolVersion) obj;
        return major == other.major && minor == other.minor;
    }

    @Override
    public String toString() {
        var predefined = Predefined.MAP.get(this);
        if (predefined != null) {
            return predefined.name();
        }
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("major", major);
        builder.append("minor", minor);
        return builder.toString();
    }

    public static boolean isSupported(ProtocolVersion protocolVersion) {
        var predefined = Predefined.MAP.get(protocolVersion);
        if (predefined == null) return false;
        return Predefined.SUPPORTED.contains(predefined);
    }
}
