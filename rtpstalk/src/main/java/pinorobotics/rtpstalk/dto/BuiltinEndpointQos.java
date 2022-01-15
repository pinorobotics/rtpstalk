package pinorobotics.rtpstalk.dto;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.stream.Collectors;

public class BuiltinEndpointQos {

	static enum Flags {
		BEST_EFFORT_PARTICIPANT_MESSAGE_DATA_READER(0),
		UNKNOWN(-1);
		
		static final Map<Integer, Flags> MAP = Arrays.stream(Flags.values())
				.collect(Collectors.toMap(k -> k.position, v -> v));
		private int position;

		Flags(int position) {
			this.position = position;
		}
	}

	public long value;

	@Override
	public String toString() {
		var set = BitSet.valueOf(new long[]{value});
		var str = set.stream()
			.mapToObj(pos -> Flags.MAP.getOrDefault(pos, Flags.UNKNOWN))
			.map(Flags::name)
			.collect(Collectors.joining(" | "));
		return str;
	}
	
}
