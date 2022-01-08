package pinorobotics.rtpstalk.dto.submessages;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.stream.Collectors;

public class BuiltinEndpointSet {

	static enum Flags {
		DISC_BUILTIN_ENDPOINT_PARTICIPANT_ANNOUNCER(0),
		DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR(1),
		DISC_BUILTIN_ENDPOINT_PUBLICATIONS_ANNOUNCER(2),
		DISC_BUILTIN_ENDPOINT_PUBLICATIONS_DETECTOR(3),
		DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_ANNOUNCER(4),
		DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_DETECTOR(5),
		/* The following have been deprecated in version 2.4 of the
		specification. These bits should not be used by versions of the
		protocol equal to or newer than the deprecated version unless
		they are used with the same meaning as in versions prior to the
		deprecated version.*/
		DISC_BUILTIN_ENDPOINT_PARTICIPANT_PROXY_ANNOUNCER(6),
		DISC_BUILTIN_ENDPOINT_PARTICIPANT_PROXY_DETECTOR(7),
		DISC_BUILTIN_ENDPOINT_PARTICIPANT_STATE_ANNOUNCER(8),
		DISC_BUILTIN_ENDPOINT_PARTICIPANT_STATE_DETECTOR(9),

		BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_WRITER(10),
		BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER(11),
		/* Bits 12-15 have been reserved by the DDS-Xtypes 1.2 Specification
		and future revisions thereof.
		Bits 16-27 have been reserved by the DDS-Security 1.1 Specification
		and future revisions thereof.
		*/
		DISC_BUILTIN_ENDPOINT_TOPICS_ANNOUNCER(28),
		DISC_BUILTIN_ENDPOINT_TOPICS_DETECTOR(29),

		UNKNOWN(-1);
		
		static final Map<Integer, Flags> MAP = Arrays.stream(Flags.values())
				.collect(Collectors.toMap(k -> k.position, v -> v));
		private int position;

		Flags(int position) {
			this.position = position;
		}
	}
	
	public int value;

	@Override
	public String toString() {
		var set = BitSet.valueOf(new long[]{value});
		var buf = new StringBuilder();
		var str = set.stream()
			.mapToObj(pos -> Flags.MAP.getOrDefault(pos, Flags.UNKNOWN))
			.map(Flags::name)
			.collect(Collectors.joining(" | "));
		return str;
	}
	
}
