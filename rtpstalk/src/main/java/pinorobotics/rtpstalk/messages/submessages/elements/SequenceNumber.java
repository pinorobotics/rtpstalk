package pinorobotics.rtpstalk.messages.submessages.elements;

import id.xfunction.XJsonStringBuilder;

public class SequenceNumber implements Comparable<SequenceNumber> {

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
}
