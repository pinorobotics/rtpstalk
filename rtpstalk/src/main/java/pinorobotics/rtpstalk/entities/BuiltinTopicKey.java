package pinorobotics.rtpstalk.entities;

import java.util.Arrays;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class BuiltinTopicKey {
	
	@Streamed
	public int[] value = new int[3];

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", Arrays.toString(value));
		return builder.toString();
	}
}
