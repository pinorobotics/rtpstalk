package pinorobotics.rtpstalk.dto;

import id.xfunction.XJsonStringBuilder;

public class Sequence {

	public int length;
	
	public byte[] data;

	public Sequence() {

	}
	
	public Sequence(byte[] data) {
		this.length = data.length;
		this.data = data;
	}

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("len", length);
		builder.append("data", data);
		return builder.toString();
	}
}
