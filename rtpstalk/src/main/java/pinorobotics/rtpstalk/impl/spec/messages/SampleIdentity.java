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
import java.util.List;
import java.util.Objects;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class SampleIdentity implements HasStreamedFields {
    static final List<String> STREAMED_FIELDS = List.of("writerGuid", "sequenceNumber");
    public static final int SIZE = Guid.SIZE + SequenceNumber.SIZE;

    public Guid writerGuid;

    public SequenceNumber sequenceNumber;

    public SampleIdentity() {}

    public SampleIdentity(Guid writerGuid, SequenceNumber sequenceNumber) {
        this.writerGuid = writerGuid;
        this.sequenceNumber = sequenceNumber;
    }

    public Guid getWriterGuid() {
        return writerGuid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(writerGuid, sequenceNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SampleIdentity other = (SampleIdentity) obj;
        return Objects.equals(writerGuid, other.writerGuid)
                && Objects.equals(sequenceNumber, other.sequenceNumber);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("writerGuid", writerGuid);
        builder.append("sequenceNumber", sequenceNumber);
        return builder.toString();
    }
}
