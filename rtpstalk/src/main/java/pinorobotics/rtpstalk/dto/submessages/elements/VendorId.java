package pinorobotics.rtpstalk.dto.submessages.elements;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import id.xfunction.XJsonStringBuilder;

public class VendorId {

	public static enum Predefined {
		RTPSTALK(new VendorId(0xca, 0xfe));
		
		static final Map<VendorId, Predefined> MAP = Arrays.stream(Predefined.values())
				.collect(Collectors.toMap(k -> k.value, v -> v));
		private VendorId value;

		Predefined(VendorId value) {
			this.value = value;
		}
		
		public VendorId getValue() {
			return value;
		}
	}

	public byte[] value = new byte[2];
	
	public VendorId() {
		
	}
	
	public VendorId(int a, int b) {
		this.value = new byte[] {(byte)a, (byte) b};
	}
	
	@Override
	public String toString() {
		var predefined = Predefined.MAP.get(this);
		if (predefined != null) {
			return predefined.name();
		}
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", Arrays.toString(value));
		return builder.toString();
	}
}
