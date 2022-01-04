package pinorobotics.rtpstalk.entities;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import id.kineticstreamer.annotations.Streamed;

public class BuiltinEndpointSet {

	enum Predefined {
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
		private int position;

		Predefined(int i) {
			this.position = i;
		}
	}
	
	static Map<Integer, Predefined> map = new HashMap<>();
	static {
		for (var t: Predefined.values()) map.put(t.position, t);
	}
	
	@Streamed
	public int value;
	
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
		BuiltinEndpointSet other = (BuiltinEndpointSet) obj;
		return value == other.value;
	}

	@Override
	public String toString() {
		var set = BitSet.valueOf(new long[]{value});
		var buf = new StringBuilder();
		var str = set.stream()
			.mapToObj(pos -> map.getOrDefault(pos, Predefined.UNKNOWN))
			.map(Predefined::name)
			.collect(Collectors.joining(" | "));
		return str;
	}
	
}
