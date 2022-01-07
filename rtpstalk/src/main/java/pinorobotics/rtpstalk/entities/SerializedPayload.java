package pinorobotics.rtpstalk.entities;

import id.xfunction.XJsonStringBuilder;

public class SerializedPayload implements SubmessageElement {

	public SerializedPayloadHeader serializedPayloadHeader;
	
	public transient Payload payload;
	
	public SerializedPayload() {
		
	}
	
	public SerializedPayload(SerializedPayloadHeader serializedPayloadHeader, Payload payload) {
		this.serializedPayloadHeader = serializedPayloadHeader;
		this.payload = payload;
	}

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("serializedPayloadHeader", serializedPayloadHeader);
		builder.append("payload", payload);
		return builder.toString();
	}

}
