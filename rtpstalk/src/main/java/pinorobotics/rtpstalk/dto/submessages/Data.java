package pinorobotics.rtpstalk.dto.submessages;

import java.util.List;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.dto.submessages.elements.SequenceNumber;

public class Data extends Submessage<SerializedPayload> {
	
	public short extraFlags;
	
	public short octetsToInlineQos;
	
	public EntityId readerId;
	
	public EntityId writerId;
	
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
		return (getFlagsInternal() & 2) != 0;
	}

	public boolean isData() {
		return (getFlagsInternal() & 4) != 0;
	}

	public boolean isKey() {
		return (getFlagsInternal() & 8) != 0;
	}

	public boolean isNonStandardPayloadFlag() {
		return (getFlagsInternal() & 10) != 0;
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

	@Override
	public List<SerializedPayload> getSubmessageElements() {
		return List.of(serializedPayload);
	}

	@Override
	public int getLength() {
		return 0;
	}
	
}
