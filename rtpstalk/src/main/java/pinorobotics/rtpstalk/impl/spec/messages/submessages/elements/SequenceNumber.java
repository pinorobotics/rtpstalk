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
package pinorobotics.rtpstalk.impl.spec.messages.submessages.elements;

import id.xfunction.XJsonStringBuilder;
import java.util.Objects;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SequenceNumber implements Comparable<SequenceNumber>, HasStreamedFields {
    public static final int SIZE = Integer.BYTES * 2;
    public static final SequenceNumber MIN = new SequenceNumber(0);
    public static final SequenceNumber MAX = new SequenceNumber(Long.MAX_VALUE);
    public static final SequenceNumber SEQUENCENUMBER_UNKNOWN =
            new SequenceNumber(0xffffffff00000000L);

    /** Composite value of high and low which we pack to long */
    public long value;

    public SequenceNumber() {}

    public SequenceNumber(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", value);
        return builder.toString();
    }

    @Override
    public int compareTo(SequenceNumber o) {
        return Long.compare(value, o.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SequenceNumber other = (SequenceNumber) obj;
        return value == other.value;
    }
}
