package pinorobotics.rtpstalk.dto;

import java.util.List;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.dto.submessages.Header;
import pinorobotics.rtpstalk.dto.submessages.Submessage;

public record RtpsMessage(Header header, List<Submessage<?>> submessages) {

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("header", header);
		builder.append("submessages", submessages);
		return builder.toString();
	}
}
