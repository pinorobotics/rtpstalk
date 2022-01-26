package pinorobotics.rtpstalk.messages.submessages.elements;

import id.xfunction.XJsonStringBuilder;
import java.util.Objects;

public class SequenceNumber implements Comparable<SequenceNumber> {

    public static final SequenceNumber MIN = new SequenceNumber(Long.MIN_VALUE);
    public static final SequenceNumber MAX = new SequenceNumber(Long.MAX_VALUE);

    public long value;

    public SequenceNumber() {

    }

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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SequenceNumber other = (SequenceNumber) obj;
        return value == other.value;
    }

}
