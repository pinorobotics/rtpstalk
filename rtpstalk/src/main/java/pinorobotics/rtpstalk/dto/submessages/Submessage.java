package pinorobotics.rtpstalk.dto.submessages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import id.xfunction.XJsonStringBuilder;

/**
 * Each RTPS Message consists of a variable number of RTPS Submessage parts.
 */
public abstract class Submessage {
	
	/**
	 * The SubmessageHeader identifies the kind of Submessage and the
	 * optional elements within that Submessage.
	 */
	public SubmessageHeader submessageHeader;

	public boolean isLittleEndian() {
		return (getFlagsInternal() & 1) == 1;
	}
	
	protected byte getFlagsInternal() {
		return submessageHeader.submessageFlag;
	}

	public List<String> getFlags() {
		var flags = new ArrayList<String>();
		if (isLittleEndian())
			flags.add("LittleEndian");
		else
			flags.add("BigEndian");
		return flags;
	}
	
	protected Object[] getAdditionalFields() {
		return new Object[0];
	}

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("submessageHeader", submessageHeader);
		builder.append("flags", getFlags());
		builder.append(getAdditionalFields());
		return builder.toString();
	}

	public static Predicate<Submessage> filterBySubmessageKind(SubmessageKind kind) {
		return submessage -> submessage.submessageHeader.submessageKind.equals(kind);
	}
	
}
