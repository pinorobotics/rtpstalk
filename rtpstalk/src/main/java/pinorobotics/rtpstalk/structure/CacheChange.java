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
package pinorobotics.rtpstalk.structure;

import id.xfunction.XJsonStringBuilder;
import java.util.Objects;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.Payload;

public class CacheChange<D extends Payload> {

    private Guid writerGuid;
    private long sequenceNumber;
    private D dataValue;

    public CacheChange(Guid writerGuid, long sequenceNumber, D dataValue) {
        this.writerGuid = writerGuid;
        this.sequenceNumber = sequenceNumber;
        this.dataValue = dataValue;
    }

    /** The Guid that identifies the RTPS Writer that made the change */
    public Guid getWriterGuid() {
        return writerGuid;
    }

    /** Sequence number assigned by the RTPS Writer to uniquely identify the change */
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * The data value associated with the change. Depending on the kind of CacheChange, there may be
     * no associated data.
     */
    public D getDataValue() {
        return dataValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceNumber, writerGuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CacheChange other = (CacheChange) obj;
        return Objects.equals(sequenceNumber, other.sequenceNumber)
                && Objects.equals(writerGuid, other.writerGuid);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("sequenceNumber", sequenceNumber);
        builder.append("hasData", dataValue != null);
        return builder.toString();
    }
}
