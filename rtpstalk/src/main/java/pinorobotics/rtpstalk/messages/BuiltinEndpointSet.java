package pinorobotics.rtpstalk.messages;

import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BuiltinEndpointSet {

    public static enum Endpoint {
        DISC_BUILTIN_ENDPOINT_PARTICIPANT_ANNOUNCER(0),
        DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR(1),
        DISC_BUILTIN_ENDPOINT_PUBLICATIONS_ANNOUNCER(2),
        DISC_BUILTIN_ENDPOINT_PUBLICATIONS_DETECTOR(3),
        DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_ANNOUNCER(4),
        DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_DETECTOR(5),

        /**
         * The following have been deprecated in version 2.4 of the specification. These
         * bits should not be used by versions of the protocol equal to or newer than
         * the deprecated version unless they are used with the same meaning as in
         * versions prior to the deprecated version.
         */
        DISC_BUILTIN_ENDPOINT_PARTICIPANT_PROXY_ANNOUNCER(6),
        DISC_BUILTIN_ENDPOINT_PARTICIPANT_PROXY_DETECTOR(7),
        DISC_BUILTIN_ENDPOINT_PARTICIPANT_STATE_ANNOUNCER(8),
        DISC_BUILTIN_ENDPOINT_PARTICIPANT_STATE_DETECTOR(9),

        BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_WRITER(10),
        BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER(11),

        /**
         * Bits 12-15 have been reserved by the DDS-Xtypes 1.2 Specification and future
         * revisions thereof.
         */

        /**
         * Bits 16-27 have been reserved by the DDS-Security 1.1 Specification and
         * future revisions thereof.
         */
        SECURE_PUBLICATION_WRITER(16),
        SECURE_PUBLICATION_READER(17),
        PARTICIPANT_SECURE_READER(18),
        SECURE_SUBSCRIPTION_WRITER(19),
        SECURE_SUBSCRIPTION_READER(20),
        SECURE_PARTICIPANT_MESSAGE_WRITER(21),
        SECURE_PARTICIPANT_MESSAGE_READER(22),
        PARTICIPANT_STATELESS_MESSAGE_WRITER(23),
        PARTICIPANT_STATELESS_MESSAGE_READER(24),
        SECURE_PARTICIPANT_VOLATILE_MESSAGE_WRITER(25),
        SECURE_PARTICIPANT_VOLATILE_MESSAGE_READER(26),
        PARTICIPANT_SECURE_WRITER(27),

        DISC_BUILTIN_ENDPOINT_TOPICS_ANNOUNCER(28),
        DISC_BUILTIN_ENDPOINT_TOPICS_DETECTOR(29),

        UNKNOWN(-1);

        static final Map<Integer, Endpoint> MAP = Arrays.stream(Endpoint.values())
                .collect(Collectors.toMap(k -> k.position, v -> v));
        private int position;

        Endpoint(int position) {
            this.position = position;
        }
    }

    public static final BuiltinEndpointSet ALL = new BuiltinEndpointSet(EnumSet.allOf(Endpoint.class));

    public int value;

    public BuiltinEndpointSet() {

    }

    public BuiltinEndpointSet(EnumSet<Endpoint> set) {
        var bset = new BitSet();
        set.stream()
                .filter(Predicate.isEqual(Endpoint.UNKNOWN).negate())
                .forEach(p -> bset.set(p.position));
        value = (int) bset.toLongArray()[0];
    }

    public boolean hasEndpoint(Endpoint endpoint) {
        return new BitSet(value).get(endpoint.position);
    }

    @Override
    public String toString() {
        var set = BitSet.valueOf(new long[] { value });
        var str = set.stream()
                .mapToObj(pos -> Endpoint.MAP.getOrDefault(pos, Endpoint.UNKNOWN))
                .map(Endpoint::name)
                .collect(Collectors.joining(" | "));
        return str;
    }

}
