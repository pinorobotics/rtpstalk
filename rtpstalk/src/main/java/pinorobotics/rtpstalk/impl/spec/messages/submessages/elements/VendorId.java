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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class VendorId implements HasStreamedFields {

    public static enum Predefined {
        RTPSTALK(new VendorId(0xca, 0xfe)),
        CYCLONEDDS(new VendorId(0x01, 0x10)),
        FASTRTPS(new VendorId(0x01, 0x0f));

        private static final Map<VendorId, Predefined> MAP =
                Arrays.stream(Predefined.values()).collect(Collectors.toMap(k -> k.value, v -> v));
        private VendorId value;

        Predefined(VendorId value) {
            this.value = value;
        }

        public VendorId getValue() {
            return value;
        }

        public static Optional<Predefined> find(VendorId vendorId) {
            return Optional.ofNullable(Predefined.MAP.get(vendorId));
        }
    }

    public byte[] value = new byte[2];

    public VendorId() {}

    public VendorId(int a, int b) {
        this.value = new byte[] {(byte) a, (byte) b};
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        VendorId other = (VendorId) obj;
        return Arrays.equals(value, other.value);
    }

    @Override
    public String toString() {
        return Predefined.find(this)
                .map(Enum::name)
                .orElseGet(
                        () -> {
                            XJsonStringBuilder builder = new XJsonStringBuilder(this);
                            builder.append("value", Arrays.toString(value));
                            return builder.toString();
                        });
    }

    public static Optional<VendorId.Predefined> findVendorId(byte[] value) {
        return VendorId.Predefined.find(new VendorId(value[0], value[1]));
    }
}
