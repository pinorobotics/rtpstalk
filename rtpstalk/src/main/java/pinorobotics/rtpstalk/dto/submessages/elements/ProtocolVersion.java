package pinorobotics.rtpstalk.dto.submessages.elements;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import id.xfunction.XJsonStringBuilder;

public class ProtocolVersion {

	public static enum Predefined {
		Version_2_3(new ProtocolVersion(2, 3));
		
		static final Map<ProtocolVersion, Predefined> MAP = Arrays.stream(Predefined.values())
				.collect(Collectors.toMap(k -> k.value, v -> v));
		private ProtocolVersion value;

		Predefined(ProtocolVersion value) {
			this.value = value;
		}
		
		public ProtocolVersion getValue() {
			return value;
		}
	}
	
	public byte major;
	
	public byte minor;
	
	public ProtocolVersion() {

	}
	
	public ProtocolVersion(int major, int minor) {
		this.major = (byte) major;
		this.minor = (byte) minor;
	}

	@Override
	public int hashCode() {
		return Objects.hash(major, minor);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProtocolVersion other = (ProtocolVersion) obj;
		return major == other.major && minor == other.minor;
	}

	@Override
	public String toString() {
		var predefined = Predefined.MAP.get(this);
		if (predefined != null) {
			return predefined.name();
		}
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("major", major);
		builder.append("minor", minor);
		return builder.toString();
	}
	
}
