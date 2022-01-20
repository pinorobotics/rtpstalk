package pinorobotics.rtpstalk.messages.submessages.elements;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import id.xfunction.XJsonStringBuilder;

public class Timestamp implements SubmessageElement {

    public static enum Predefined {
        TIME_ZERO(new Timestamp(0, 0)),
        TIME_INVALID(new Timestamp(0xffffffff, 0xffffffff)),
        TIME_INFINITE(new Timestamp(0xffffffff, 0xfffffffe));

        static final Map<Timestamp, Predefined> MAP = Arrays.stream(Predefined.values())
                .collect(Collectors.toMap(k -> k.value, v -> v));
        private Timestamp value;

        Predefined(Timestamp value) {
            this.value = value;
        }

        public Timestamp getValue() {
            return value;
        }
    }

    public int seconds;

    /**
     * Time in sec/2^32
     */
    public int fraction;

    public Timestamp() {
        // TODO Auto-generated constructor stub
    }

    public Timestamp(long seconds, long fraction) {
        this.seconds = (int) seconds;
        this.fraction = (int) fraction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fraction, seconds);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Timestamp other = (Timestamp) obj;
        return fraction == other.fraction && seconds == other.seconds;
    }

    @Override
    public String toString() {
        var predefined = Predefined.MAP.get(this);
        if (predefined != null) {
            return predefined.name();
        }
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("seconds", seconds);
        builder.append("fraction", Integer.toUnsignedLong(fraction));
        return builder.toString();
    }

    public static Timestamp now() {
        var secs = Instant.now().getEpochSecond();
        var fraction = secs / (1 << 31);
        return new Timestamp(secs, fraction);
    }

}
