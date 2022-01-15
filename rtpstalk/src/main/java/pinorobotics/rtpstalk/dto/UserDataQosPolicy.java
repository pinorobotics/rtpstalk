package pinorobotics.rtpstalk.dto;

import id.xfunction.XJsonStringBuilder;

public class UserDataQosPolicy {

	public Sequence value;

	public UserDataQosPolicy() {
		
	}
	
	public UserDataQosPolicy(Sequence value) {
		this.value = value;
	}

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", value);
		return builder.toString();
	}
}
