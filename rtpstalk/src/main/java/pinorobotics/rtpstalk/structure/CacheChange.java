package pinorobotics.rtpstalk.structure;

import id.xfunction.XJsonStringBuilder;
import java.util.Objects;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;

public class CacheChange {

    private Guid writerGuid;
    private SequenceNumber sequenceNumber;
    private Payload dataValue;

    public CacheChange(Guid writerGuid, SequenceNumber sequenceNumber, Payload dataValue) {
        this.writerGuid = writerGuid;
        this.sequenceNumber = sequenceNumber;
        this.dataValue = dataValue;
    }

    /**
     * The Guid that identifies the RTPS Writer that made the change
     */
    public Guid getWriterGuid() {
        return writerGuid;
    }

    /**
     * Sequence number assigned by the RTPS Writer to uniquely identify the change
     */
    public SequenceNumber getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * The data value associated with the change. Depending on the kind of
     * CacheChange, there may be no associated data.
     */
    public Payload getDataValue() {
        return dataValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceNumber, writerGuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
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
