package pinorobotics.rtpstalk.messages.submessages.elements;

import id.xfunction.XJsonStringBuilder;
import java.util.Objects;

public class SequenceNumber implements Comparable<SequenceNumber> {

    public static final SequenceNumber MIN = new SequenceNumber(Integer.MIN_VALUE, -Integer.MIN_VALUE);
    public static final SequenceNumber MAX = new SequenceNumber(Integer.MAX_VALUE, -Integer.MAX_VALUE);

    public int high;

    public int low;

    public SequenceNumber() {

    }

    public SequenceNumber(int high, int low) {
        this.high = high;
        this.low = low;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("high", high);
        builder.append("low", low);
        return builder.toString();
    }

    @Override
    public int compareTo(SequenceNumber o) {
        if (high < o.high)
            return -1;
        if (high == o.high)
            return Integer.compare(low, o.low);
        return 1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(high, low);
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
        return high == other.high && low == other.low;
    }

}
