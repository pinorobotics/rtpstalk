package pinorobotics.rtpstalk.entities;

import java.util.List;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class InfoTimestamp extends SubmessageElement {
	
	@Streamed
	public Timestamp timestamp;
	
	public InfoTimestamp() {
		// TODO Auto-generated constructor stub
	}
	
	public InfoTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public List<String> getFlags() {
		var flags = super.getFlags();
		if (isInvalidate()) flags.add("InvalidateFlag");
		return flags;
	}

	/**
	 * Subsequent Submessages should not be considered to have a valid timestamp.
	 */
	private boolean isInvalidate() {
		return (flags & 2) != 0;
	}

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("super", super.toString());
		builder.append("timestamp", timestamp);
		return builder.toString();
	}
	
	public static InfoTimestamp now() {
		return new InfoTimestamp(Timestamp.now());
	}
}
