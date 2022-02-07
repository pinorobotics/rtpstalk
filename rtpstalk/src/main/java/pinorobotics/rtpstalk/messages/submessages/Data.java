package pinorobotics.rtpstalk.messages.submessages;

import java.util.List;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.transport.io.LengthCalculator;

public class Data extends Submessage {

    public short extraFlags;

    /**
     * Contains the number of octets starting from the first octet immediately
     * following this field until the first octet of the inlineQos
     * SubmessageElement. If the inlineQos SubmessageElement is not present (i.e.,
     * the InlineQosFlag is not set), then octetsToInlineQos contains the offset to
     * the next field after the inlineQos.
     * 
     * Currently inlineQos is no supported.
     */
    public short octetsToInlineQos;

    /**
     * Identifies the RTPS Reader entity that is being informed of the change to the
     * data-object
     */
    public EntityId readerId;

    /**
     * Identifies the RTPS Writer entity that made the change to the data-object
     */
    public EntityId writerId;

    /**
     * Uniquely identifies the change and the relative order for all changes made by
     * the RTPS Writer identified by the writerGuid. Each change gets a consecutive
     * sequence number. Each RTPS Writer maintains is own sequence number
     */
    public SequenceNumber writerSN;

    public transient SerializedPayload serializedPayload;

    public Data() {

    }

    public Data(int flags, int extraFlags, EntityId readerId, EntityId writerId,
            SequenceNumber writerSN, SerializedPayload serializedPayload) {
        this.extraFlags = (short) extraFlags;
        this.octetsToInlineQos = (short) (LengthCalculator.getInstance().getFixedLength(EntityId.class)
                * 2
                + LengthCalculator.getInstance().getFixedLength(SequenceNumber.class));
        this.readerId = readerId;
        this.writerId = writerId;
        this.writerSN = writerSN;
        this.serializedPayload = serializedPayload;
        submessageHeader = new SubmessageHeader(SubmessageKind.Predefined.DATA.getValue(), flags,
                LengthCalculator.getInstance().calculateLength(this));
    }

    @Override
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
