package pinorobotics.rtpstalk.entities;

import id.xfunction.XJsonStringBuilder;

public class SequenceNumber {

	public int value;

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", value);
		return builder.toString();
	}
}
