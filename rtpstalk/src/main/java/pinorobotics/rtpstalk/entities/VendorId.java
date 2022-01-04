package pinorobotics.rtpstalk.entities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class VendorId {

	public static enum Predefined {
		RTPSTALK(new VendorId(0xca, 0xfe));
		
		private VendorId value;

		Predefined(VendorId value) {
			this.value = value;
		}
		
		public VendorId getValue() {
			return value;
		}
	}

	static Map<VendorId, Predefined> map = new HashMap<>();
	static {
		for (var t: Predefined.values()) map.put(t.value, t);
	}
	
	@Streamed
	public byte[] value = new byte[2];

	public VendorId() {
		// TODO Auto-generated constructor stub
	}
	
	public VendorId(int a, int b) {
		value[0] = (byte) a;
		value[1] = (byte) b;
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
		VendorId other = (VendorId) obj;
		return Arrays.equals(value, other.value);
	}

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
}
