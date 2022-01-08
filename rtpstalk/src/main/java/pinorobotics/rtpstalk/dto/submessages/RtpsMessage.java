package pinorobotics.rtpstalk.dto.submessages;

import java.util.List;

import id.xfunction.XJsonStringBuilder;

public record RtpsMessage(Header header, List<Submessage> submessages) {

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("header", header);
		builder.append("submessages", submessages);
		return builder.toString();
	}
}
