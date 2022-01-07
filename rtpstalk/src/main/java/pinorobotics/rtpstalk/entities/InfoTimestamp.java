package pinorobotics.rtpstalk.entities;

import java.util.List;

public class InfoTimestamp extends Submessage<Timestamp> {
	
	public Timestamp timestamp;
	
	public InfoTimestamp() {

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
		return (getFlagsInternal() & 2) != 0;
	}

	@Override
	public int getLength() {
		return Timestamp.getLength();
	}

	public static InfoTimestamp now() {
		return new InfoTimestamp(Timestamp.now());
	}

	@Override
	public List<Timestamp> getSubmessageElements() {
		return List.of(timestamp);
	}
}
