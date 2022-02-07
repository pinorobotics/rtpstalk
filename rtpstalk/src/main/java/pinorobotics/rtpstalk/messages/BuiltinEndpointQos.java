package pinorobotics.rtpstalk.messages;

import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BuiltinEndpointQos {

    public static enum EndpointQos {
        BEST_EFFORT_PARTICIPANT_MESSAGE_DATA_READER(0),
        UNKNOWN(-1);

        static final Map<Integer, EndpointQos> MAP = Arrays.stream(EndpointQos.values())
                .collect(Collectors.toMap(k -> k.position, v -> v));
        private int position;

        EndpointQos(int position) {
            this.position = position;
        }
    }

    public int value;

    public BuiltinEndpointQos() {
    }

    public BuiltinEndpointQos(EnumSet<EndpointQos> set) {
        var bset = new BitSet();
        set.stream()
                .filter(Predicate.isEqual(EndpointQos.UNKNOWN).negate())
                .forEach(p -> bset.set(p.position));
        value = (int) bset.toLongArray()[0];
    }

    public boolean hasFlag(EndpointQos flag) {
        return BitSet.valueOf(new long[] { value }).get(flag.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BuiltinEndpointQos other = (BuiltinEndpointQos) obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        var set = BitSet.valueOf(new long[] { value });
        var str = set.stream()
                .mapToObj(pos -> EndpointQos.MAP.getOrDefault(pos, EndpointQos.UNKNOWN))
                .map(EndpointQos::name)
                .collect(Collectors.joining(" | "));
        return str;
    }

}
