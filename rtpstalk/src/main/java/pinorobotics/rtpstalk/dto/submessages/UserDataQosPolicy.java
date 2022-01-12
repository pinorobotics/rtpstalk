package pinorobotics.rtpstalk.dto.submessages;

import id.xfunction.XJsonStringBuilder;

public class UserDataQosPolicy {

	public byte[] value;

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", value);
		return builder.toString();
	}
}
