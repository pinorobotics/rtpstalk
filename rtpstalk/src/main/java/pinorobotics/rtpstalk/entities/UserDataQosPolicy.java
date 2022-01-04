package pinorobotics.rtpstalk.entities;

import java.util.List;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class UserDataQosPolicy {

	@Streamed
	public List<Byte> value;

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", value);
		return builder.toString();
	}
}
