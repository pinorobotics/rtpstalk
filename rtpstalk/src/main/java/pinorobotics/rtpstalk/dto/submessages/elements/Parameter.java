package pinorobotics.rtpstalk.dto.submessages.elements;

import id.xfunction.XJsonStringBuilder;

public record Parameter(ParameterId parameterId, Object value) {
	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("parameterId", parameterId);
		builder.append("value", value);
		return builder.toString();
	}
}
