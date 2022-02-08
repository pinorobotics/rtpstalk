package pinorobotics.rtpstalk.messages.submessages;

import id.xfunction.XJsonStringBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RepresentationIdentifier {

    public static final int SIZE = 2;

    public static enum Predefined {
        CDR_LE(new RepresentationIdentifier(new byte[] { 0x00, 0x01 })),
        PL_CDR_BE(new RepresentationIdentifier(new byte[] { 0x00, 0x02 })),
        PL_CDR_LE(new RepresentationIdentifier(new byte[] { 0x00, 0x03 }));

        static final Map<RepresentationIdentifier, Predefined> MAP = Arrays.stream(Predefined.values())
                .collect(Collectors.toMap(k -> k.value, v -> v));
        private RepresentationIdentifier value;

        Predefined(RepresentationIdentifier value) {
            this.value = value;
        }

        public RepresentationIdentifier getValue() {
            return value;
        }
    }

    public byte[] value = new byte[SIZE];

    public RepresentationIdentifier() {

    }

    public RepresentationIdentifier(byte[] value) {
        this.value = value;
    }

    public Optional<Predefined> findPredefined() {
        return Optional.ofNullable(Predefined.MAP.get(this));
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
        RepresentationIdentifier other = (RepresentationIdentifier) obj;
        return Arrays.equals(value, other.value);
    }

    @Override
    public String toString() {
        var predefined = Predefined.MAP.get(this);
        if (predefined != null) {
            return predefined.name();
        }
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", "unknown<" + Arrays.toString(value) + ">");
        return builder.toString();
    }
}
