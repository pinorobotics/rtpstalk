package pinorobotics.rtpstalk.dto;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import id.xfunction.XJsonStringBuilder;

public class ProtocolId {

    public static enum Predefined {
        RTPS(new ProtocolId(new byte[] { 'R', 'T', 'P', 'S' }));

        static final Map<ProtocolId, Predefined> MAP = Arrays.stream(Predefined.values())
                .collect(Collectors.toMap(k -> k.value, v -> v));
        private ProtocolId value;

        Predefined(ProtocolId value) {
            this.value = value;
        }

        public ProtocolId getValue() {
            return value;
        }
    }

    public byte[] value = new byte[4];

    public ProtocolId() {

    }

    public ProtocolId(byte[] value) {
        this.value = value;
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
        ProtocolId other = (ProtocolId) obj;
        return Arrays.equals(value, other.value);
    }

    @Override
    public String toString() {
        var predefined = Predefined.MAP.get(this);
        if (predefined != null) {
            return predefined.name();
        }
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", new String(value));
        return builder.toString();
    }
}
