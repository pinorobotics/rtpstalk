package pinorobotics.rtpstalk.entities;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import id.kineticstreamer.annotations.Streamed;

public class BuiltinEndpointQos {

	enum Type {
		BEST_EFFORT_PARTICIPANT_MESSAGE_DATA_READER(0),

		UNKNOWN(-1);
		private int position;

		Type(int i) {
			this.position = i;
		}
	}
	
	static Map<Integer, Type> map = new HashMap<>();
	
	static {
		for (var t: Type.values()) map.put(t.position, t);
	}
	
	@Streamed
	public long value;
	
	@Override
	public String toString() {
		var set = BitSet.valueOf(new long[]{value});
		var buf = new StringBuilder();
		var str = set.stream()
			.mapToObj(pos -> map.getOrDefault(pos, Type.UNKNOWN))
			.map(Type::name)
			.collect(Collectors.joining(" | "));
		System.out.println(str);
		return str;
	}
	
}
