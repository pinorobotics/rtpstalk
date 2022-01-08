package pinorobotics.rtpstalk.dto.submessages;

import java.util.Arrays;

import id.xfunction.XJsonStringBuilder;

public class SerializedPayloadHeader {
	
	public RepresentationIdentifier representation_identifier;
	
	public byte[] representation_options = new byte[2];
	
	public SerializedPayloadHeader() {

	}
	
	public SerializedPayloadHeader(RepresentationIdentifier representation_identifier, byte[] representation_options) {
		this.representation_identifier = representation_identifier;
		this.representation_options = representation_options;
	}

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("representation_identifier", representation_identifier);
		builder.append("representation_options", Arrays.toString(representation_options));
		return builder.toString();
	}
}
