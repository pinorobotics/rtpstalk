package pinorobotics.rtpstalk.entities;

import id.xfunction.XJsonStringBuilder;

public class ParticipantBuiltinTopicData {

	public BuiltinTopicKey key;
	
	public UserDataQosPolicy user_data;
	
	public ParticipantBuiltinTopicData() {

	}
	
	public ParticipantBuiltinTopicData(BuiltinTopicKey key, UserDataQosPolicy user_data) {
		this.key = key;
		this.user_data = user_data;
	}

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("key", key);
		builder.append("user_data", user_data);
		return builder.toString();
	}
}
