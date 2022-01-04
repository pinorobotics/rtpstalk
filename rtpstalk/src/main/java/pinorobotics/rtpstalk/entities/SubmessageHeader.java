package pinorobotics.rtpstalk.entities;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public record SubmessageHeader(

	/**
	 * Identifies the kind of Submessage.
	 */
	@Streamed
	SubmessageKind submessageKind,
	
	@Streamed
	byte submessageFlag,

	/**
	 * octetsToNextHeader
	 */
	@Streamed
	short submessageLength) {
	
	public SubmessageHeader(SubmessageKind kind, int submessageFlag, int submessageLength) {
		this(kind, (byte)submessageFlag, (short)submessageLength);
	}

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("submessageKind", submessageKind);
		builder.append("submessageFlag", submessageFlag);
		builder.append("submessageLength", submessageLength);
		return builder.toString();
	}
}
