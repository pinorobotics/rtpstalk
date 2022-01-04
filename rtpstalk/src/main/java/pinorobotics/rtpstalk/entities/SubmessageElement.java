package pinorobotics.rtpstalk.entities;

import java.util.ArrayList;
import java.util.List;

import id.xfunction.XJsonStringBuilder;

public class SubmessageElement {
	
	protected byte flags;

	public boolean isLittleEndian() {
		return (flags & 1) == 1;
	}
	
	public void setFlags(byte flags) {
		this.flags = flags;
	}
	
	public List<String> getFlags() {
		var flags = new ArrayList<String>();
		if (isLittleEndian())
			flags.add("LittleEndian");
		else
			flags.add("BigEndian");
		return flags;
	}

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("flags", getFlags());
		return builder.toString();
	}
	
}
