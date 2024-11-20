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

import id.xfunction.Preconditions;
import java.util.Objects;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class UnsignedInt implements HasStreamedFields {
    /**
     * This is unsigned int and it should not be used directly but through getter method. It is made
     * public to make serialization possible and it required by kineticstreamer
     */
    public int value;

    public UnsignedInt() {
        // required by kineticstreamer
    }

    public UnsignedInt(long value) {
        Preconditions.isLessOrEqual(0, value, "Only positive value allowed");
        Preconditions.isLessOrEqual(value, 0xffffffffL, "Value exceeds unsigned int");
        this.value = (int) value;
    }

    public long getUnsigned() {
        return Integer.toUnsignedLong(value);
    }

    @Override
    public String toString() {
        return "" + Integer.toUnsignedLong(value);
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
        UnsignedInt other = (UnsignedInt) obj;
        return value == other.value;
    }
}
