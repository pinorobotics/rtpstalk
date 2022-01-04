package pinorobotics.rtpstalk.entities;

import java.util.Arrays;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class ProtocolId {

	public static final ProtocolId RTPS = new ProtocolId(new byte[] {'R', 'T', 'P', 'S'});
	
	@Streamed
	public byte[] value = new byte[4];

	public ProtocolId() {
	}
	
	public ProtocolId(byte[] value) {
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
		ProtocolId other = (ProtocolId) obj;
		return Arrays.equals(value, other.value);
	}

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", new String(value));
		return builder.toString();
	}
}
