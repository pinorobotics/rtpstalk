package pinorobotics.rtpstalk.entities;

import java.util.List;
import java.util.function.Predicate;

import id.xfunction.XJsonStringBuilder;

/**
 * Each RTPS Message consists of a variable number of RTPS Submessage parts.
 */
public record Submessage(
		/**
		 * The SubmessageHeader identifies the kind of Submessage and the
		 * optional elements within that Submessage.
		 */
		SubmessageHeader submessageHeader,

		List<SubmessageElement> submessageElements)
{

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("submessageHeader", submessageHeader);
		builder.append("submessageElements", submessageElements);
		return builder.toString();
	}

	public static Predicate<Submessage> filterBySubmessageKind(SubmessageKind kind) {
		return submessage -> submessage.submessageHeader().submessageKind().equals(kind);
	}
}
