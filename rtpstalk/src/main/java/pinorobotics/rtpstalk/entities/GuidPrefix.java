package pinorobotics.rtpstalk.entities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

/**
 * Uniquely identifies the Participant within the Domain
 */
public class GuidPrefix {

	private static final int SIZE = 12;

	public static enum Predefined {
		GUIDPREFIX_UNKNOWN(new GuidPrefix());
		
		private GuidPrefix value;

		Predefined(GuidPrefix value) {
			this.value = value;
		}
		
		public GuidPrefix getValue() {
			return value;
		}
	}
	
	static Map<GuidPrefix, Predefined> map = new HashMap<>();
	static {
		for (var t: Predefined.values()) map.put(t.value, t);
	}
	
	public GuidPrefix() {
		// TODO Auto-generated constructor stub
	}
	
	public GuidPrefix(byte[] value) {
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
		GuidPrefix other = (GuidPrefix) obj;
		return Arrays.equals(value, other.value);
	}

	@Streamed
	public byte[] value = new byte[SIZE];

	@Override
	public String toString() {
		var predefined = map.get(this);
		if (predefined != null) {
			return predefined.name();
		}
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", Arrays.toString(value));
		return builder.toString();
	}
	
	public static GuidPrefix generate() {
		var a = new byte[SIZE];
		new Random().nextBytes(a);
		return new GuidPrefix(a);
	}
}
