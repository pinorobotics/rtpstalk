package pinorobotics.rtpstalk.entities;

import java.util.Arrays;
import java.util.List;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class Data extends SubmessageElement {
	
	@Streamed
	public short extraFlags;
	
	@Streamed
	public short octetsToInlineQos;
	
	@Streamed
	public EntityId readerId;
	
	@Streamed
	public EntityId writerId;
	
	@Streamed
	public SequenceNumber writerSN;

	public SerializedPayload serializedPayload;
	
	public List<String> getFlags() {
		var flags = super.getFlags();
		if (isInlineQos()) flags.add("InlineQos");
		if (isData()) flags.add("Data");
		if (isKey()) flags.add("Key");
		if (isNonStandardPayloadFlag()) flags.add("NonStandardPayloadFlag");
		return flags;
	}

	public boolean isInlineQos() {
		return (flags & 2) != 0;
	}

	public boolean isData() {
		return (flags & 4) != 0;
	}

	public boolean isKey() {
		return (flags & 8) != 0;
	}

	public boolean isNonStandardPayloadFlag() {
		return (flags & 10) != 0;
	}
	
	/**
	 * See "9.4.5.3 Data Submessage"
	 */
	public int getBytesToSkip() {
		// sizeof(readerId + writerId + writerSN) == 8
		return octetsToInlineQos - 12;
	}
	
	public SerializedPayload getSerializedPayload() {
		return serializedPayload;
	}
	
	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("super", super.toString());
		builder.append("extraFlags", extraFlags);
		builder.append("octetsToInlineQos", octetsToInlineQos);
		builder.append("readerId", readerId);
		builder.append("writerId", writerId);
		builder.append("writerSN", writerSN);
		builder.append("serializedPayload", serializedPayload);
		return builder.toString();
	}
	
}
