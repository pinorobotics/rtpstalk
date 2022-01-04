package pinorobotics.rtpstalk.entities;

import java.util.Arrays;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class VendorId {

	public static final VendorId GUIDPREFIX_UNKNOWN = new VendorId();
	
	@Streamed
	public byte[] value = new byte[2];

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", Arrays.toString(value));
		return builder.toString();
	}
}
