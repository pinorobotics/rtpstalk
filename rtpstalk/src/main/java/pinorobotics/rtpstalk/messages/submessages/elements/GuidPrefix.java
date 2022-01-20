package pinorobotics.rtpstalk.messages.submessages.elements;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import id.xfunction.XJsonStringBuilder;

/**
 * Uniquely identifies the Participant within the Domain
 */
public class GuidPrefix implements SubmessageElement {

    public static final int SIZE = 12;

    public static enum Predefined {
        GUIDPREFIX_UNKNOWN(new GuidPrefix());

        static final Map<GuidPrefix, Predefined> MAP = Arrays.stream(Predefined.values())
                .collect(Collectors.toMap(k -> k.value, v -> v));
        private GuidPrefix value;

        Predefined(GuidPrefix value) {
            this.value = value;
        }

        public GuidPrefix getValue() {
            return value;
        }
    }

    public byte[] value = new byte[SIZE];

    public GuidPrefix() {
    }

    public GuidPrefix(byte[] value) {
        this.value = value;
    }

    public GuidPrefix(int hostId, int appId, int instanceId) {
        var buf = ByteBuffer.allocate(SIZE);
        buf.putInt(hostId);
        buf.putInt(appId);
        buf.putInt(instanceId);
        value = buf.array();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GuidPrefix other = (GuidPrefix) obj;
        return Arrays.equals(value, other.value);
    }

    @Override
    public String toString() {
        var predefined = Predefined.MAP.get(this);
        if (predefined != null) {
            return predefined.name();
        }
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", value);
        return builder.toString();
    }

    public static GuidPrefix generate() {
        var a = new byte[SIZE];
        new Random().nextBytes(a);
        var rtpsTalkId = VendorId.Predefined.RTPSTALK.getValue().value;
        a[0] = rtpsTalkId[0];
        a[1] = rtpsTalkId[1];
        return new GuidPrefix(a);
    }

}