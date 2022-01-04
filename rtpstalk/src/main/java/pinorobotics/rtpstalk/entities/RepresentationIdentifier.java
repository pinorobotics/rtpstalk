package pinorobotics.rtpstalk.entities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class RepresentationIdentifier {

	public static enum Predefined {
		PL_CDR_BE(new RepresentationIdentifier(new byte[]{0x00, 0x02})),
		PL_CDR_LE(new RepresentationIdentifier(new byte[]{0x00, 0x03}));
		
		private RepresentationIdentifier value;

		Predefined(RepresentationIdentifier value) {
			this.value = value;
		}
		
		public RepresentationIdentifier getValue() {
			return value;
		}
	}
	
	static Map<RepresentationIdentifier, Predefined> map = new HashMap<>();
	static {
		for (var t: Predefined.values()) map.put(t.value, t);
	}
	
	@Streamed
	public byte[] value = new byte[2];
	
	public RepresentationIdentifier(byte[] value) {
		this.value = value;
	}
	
	public RepresentationIdentifier() {
		// TODO Auto-generated constructor stub
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
		var predefined = map.get(this);
		if (predefined != null) {
			return predefined.name();
		}
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", "unknown<" + Arrays.toString(value) + ">");
		return builder.toString();
	}
}
