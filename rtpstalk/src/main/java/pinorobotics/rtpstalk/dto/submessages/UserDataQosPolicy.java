package pinorobotics.rtpstalk.dto.submessages;

import java.util.List;

import id.xfunction.XJsonStringBuilder;

public class UserDataQosPolicy {

	public List<Byte> value = List.of();

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", value);
		return builder.toString();
	}
}
