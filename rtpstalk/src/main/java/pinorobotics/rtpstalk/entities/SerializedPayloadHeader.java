package pinorobotics.rtpstalk.entities;

import java.util.Arrays;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class SerializedPayloadHeader {
	
	@Streamed
	public RepresentationIdentifier representation_identifier;
	
	@Streamed
	public byte[] representation_options = new byte[2];

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("representation_identifier", representation_identifier);
		builder.append("representation_options", Arrays.toString(representation_options));
		return builder.toString();
	}
}
