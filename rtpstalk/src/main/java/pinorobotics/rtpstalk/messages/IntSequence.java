package pinorobotics.rtpstalk.messages;

import id.xfunction.XJsonStringBuilder;

public class IntSequence implements Sequence {

    public int length;

    public int[] data = new int[0];

    public IntSequence() {

    }

    public IntSequence(int len) {
        this(new int[len]);
    }

    public IntSequence(int[] data) {
        this.length = data.length;
        this.data = data;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("len", length);
        builder.append("data", data);
        return builder.toString();
    }
}
