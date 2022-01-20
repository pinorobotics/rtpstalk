package pinorobotics.rtpstalk.messages.submessages.elements;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import id.xfunction.XJsonStringBuilder;

public class VendorId {

    public static enum Predefined {
        RTPSTALK(new VendorId(0xca, 0xfe)),
        FASTRTPS(new VendorId(0x01, 0x0f));

        static final Map<VendorId, Predefined> MAP = Arrays.stream(Predefined.values())
                .collect(Collectors.toMap(k -> k.value, v -> v));
        private VendorId value;

        Predefined(VendorId value) {
            this.value = value;
        }

        public VendorId getValue() {
            return value;
        }
    }

    public byte[] value = new byte[2];

    public VendorId() {

    }

    public VendorId(int a, int b) {
        this.value = new byte[] { (byte) a, (byte) b };
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
        VendorId other = (VendorId) obj;
        return Arrays.equals(value, other.value);
    }

    @Override
    public String toString() {
        var predefined = Predefined.MAP.get(this);
        if (predefined != null) {
            return predefined.name();
        }
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", Arrays.toString(value));
        return builder.toString();
    }
}
