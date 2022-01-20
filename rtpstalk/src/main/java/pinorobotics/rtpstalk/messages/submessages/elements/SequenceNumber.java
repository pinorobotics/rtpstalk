package pinorobotics.rtpstalk.messages.submessages.elements;

import id.xfunction.XJsonStringBuilder;

public class SequenceNumber {

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
}
