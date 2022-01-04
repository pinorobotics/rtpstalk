package pinorobotics.rtpstalk.entities;

import java.util.Arrays;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

/**
 * Uniquely identifies the Participant within the Domain
 */
public class GuidPrefix {

	public static final GuidPrefix GUIDPREFIX_UNKNOWN = new GuidPrefix();
	
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
	public byte[] value = new byte[12];

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", Arrays.toString(value));
		return builder.toString();
	}
}
