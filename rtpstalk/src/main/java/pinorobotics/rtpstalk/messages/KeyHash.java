package pinorobotics.rtpstalk.messages;

import id.xfunction.XAsserts;
import id.xfunction.XByte;
import id.xfunction.XJsonStringBuilder;

public class KeyHash implements Sequence {

    public static final int SIZE = 16;

    public byte[] value = new byte[SIZE];

    public KeyHash() {

    }

    public KeyHash(int... value) {
        XAsserts.assertEquals(SIZE, value.length, "Value size is wrong");
        this.value = XByte.castToByteArray(value);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", value);
        return builder.toString();
    }
}
