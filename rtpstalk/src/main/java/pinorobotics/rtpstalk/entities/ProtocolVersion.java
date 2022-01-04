package pinorobotics.rtpstalk.entities;

import java.util.Objects;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class ProtocolVersion {

	public static final ProtocolVersion Version_2_3 = new ProtocolVersion(2, 3);

	@Streamed
	public byte major;
	
	@Streamed
	public byte minor;

	public ProtocolVersion() {
		// TODO Auto-generated constructor stub
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
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("major", major);
		builder.append("minor", minor);
		return builder.toString();
	}
	
}
