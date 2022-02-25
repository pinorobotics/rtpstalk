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

    public KeyHash(Guid guid) {
        value = new byte[SIZE];
        System.arraycopy(guid.guidPrefix.value, 0, value, 0, guid.guidPrefix.value.length);
        System.arraycopy(guid.entityId.entityKey, 0, value, guid.guidPrefix.value.length,
                guid.entityId.entityKey.length);
        value[value.length - 1] = guid.entityId.entityKind;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", value);
        return builder.toString();
    }
}
