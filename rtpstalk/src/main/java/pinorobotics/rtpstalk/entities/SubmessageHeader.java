package pinorobotics.rtpstalk.entities;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class SubmessageHeader {

	/**
	 * Identifies the kind of Submessage.
	 */
	@Streamed
	public SubmessageKind submessageKind;
	
	@Streamed
	public byte submessageFlag;

	/**
	 * octetsToNextHeader
	 */
	@Streamed
	public short submessageLength;
	
	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("submessageKind", submessageKind);
		builder.append("submessageFlag", submessageFlag);
		builder.append("submessageLength", submessageLength);
		return builder.toString();
	}
}
