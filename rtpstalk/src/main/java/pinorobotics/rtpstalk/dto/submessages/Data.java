package pinorobotics.rtpstalk.dto.submessages;

import java.util.List;

import pinorobotics.rtpstalk.dto.submessages.elements.EntityId;
import pinorobotics.rtpstalk.dto.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.io.LengthCalculator;

public class Data extends Submessage {

    private static final short PAYLOAD_OFFSET = (short) (LengthCalculator.getInstance().getFixedLength(EntityId.class)
            * 2
            + LengthCalculator.getInstance().getFixedLength(SequenceNumber.class));

    public short extraFlags;

    public short octetsToInlineQos;

    public EntityId readerId;

    public EntityId writerId;

    public SequenceNumber writerSN;

    public transient SerializedPayload serializedPayload;

    public Data() {

    }

    public Data(int flags, int extraFlags, EntityId readerId, EntityId writerId,
            SequenceNumber writerSN, SerializedPayload serializedPayload) {
        this.extraFlags = (short) extraFlags;
        this.octetsToInlineQos = PAYLOAD_OFFSET;
        this.readerId = readerId;
        this.writerId = writerId;
        this.writerSN = writerSN;
        this.serializedPayload = serializedPayload;
        submessageHeader = new SubmessageHeader(SubmessageKind.Predefined.DATA.getValue(), flags,
                LengthCalculator.getInstance().calculateLength(this));
    }

    public List<String> getFlags() {
        var flags = super.getFlags();
        if (isInlineQos())
            flags.add("InlineQos");
        if (isData())
            flags.add("Data");
        if (isKey())
            flags.add("Key");
        if (isNonStandardPayloadFlag())
            flags.add("NonStandardPayloadFlag");
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
        return octetsToInlineQos - PAYLOAD_OFFSET;
    }

    public SerializedPayload getSerializedPayload() {
        return serializedPayload;
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] {
                "extraFlags", extraFlags,
                "octetsToInlineQos", octetsToInlineQos,
                "readerId", readerId,
                "writerId", writerId,
                "writerSN", writerSN,
                "serializedPayload", serializedPayload };
    }
}
