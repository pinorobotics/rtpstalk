package pinorobotics.rtpstalk.dto.submessages;

import id.xfunction.XJsonStringBuilder;

public class SubmessageHeader {

	/**
	 * Identifies the kind of Submessage.
	 */
	public SubmessageKind submessageKind;
	
	public byte submessageFlag;

	/**
	 * octetsToNextHeader
	 */
	public short submessageLength;
	
	public SubmessageHeader() {

	}

	public SubmessageHeader(SubmessageKind kind, int submessageFlag, int submessageLength) {
		this.submessageKind = kind;
		this.submessageFlag = (byte)submessageFlag;
		this.submessageLength = (short)submessageLength;
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
