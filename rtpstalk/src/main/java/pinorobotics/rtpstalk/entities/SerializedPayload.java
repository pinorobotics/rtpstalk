package pinorobotics.rtpstalk.entities;

import id.xfunction.XJsonStringBuilder;

public record SerializedPayload(SerializedPayloadHeader serializedPayloadHeader, Payload payload) {

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("serializedPayloadHeader", serializedPayloadHeader);
		builder.append("payload", payload);
		return builder.toString();
	}

}
