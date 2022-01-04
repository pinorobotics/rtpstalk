package pinorobotics.rtpstalk.entities;

import java.util.Arrays;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class ParticipantBuiltinTopicData {

	@Streamed
	public BuiltinTopicKey key;
	
	@Streamed
	public UserDataQosPolicy user_data;

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("key", key);
		builder.append("user_data", user_data);
		return builder.toString();
	}
}
