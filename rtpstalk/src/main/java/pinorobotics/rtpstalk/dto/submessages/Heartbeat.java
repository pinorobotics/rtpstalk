package pinorobotics.rtpstalk.dto.submessages;

import java.util.List;

import pinorobotics.rtpstalk.dto.submessages.elements.Count;
import pinorobotics.rtpstalk.dto.submessages.elements.EntityId;
import pinorobotics.rtpstalk.dto.submessages.elements.SequenceNumber;

public class Heartbeat extends Submessage {

    /**
     * Identifies the Reader Entity that is being informed of the availability of a
     * set of sequence numbers. Can be set to {@link EntityId#ENTITYID_UNKNOWN} to
     * indicate all readers for the writer that sent the message.
     */
    public EntityId readerId;

    /**
     * Identifies the Writer Entity to which the range of sequence numbers applies.
     */
    public EntityId writerId;

    /**
     * If samples are available in the Writer, identifies the first (lowest)
     * sequence number that is available in the Writer. If no samples are available
     * in the Writer, identifies the lowest sequence number that is yet to be
     * written by the Writer.
     */
    public SequenceNumber firstSN;

    /**
     * Identifies the last (highest) sequence number that the Writer has ever
     * written
     */
    public SequenceNumber lastSN;

    /**
     * A counter that is incremented each time a new Heartbeat message is sent.
     * Provides the means for a Reader to detect duplicate Heartbeat messages that
     * can result from the presence of redundant communication paths
     */
    public Count count;

    public Heartbeat() {

    }

    public List<String> getFlags() {
        var flags = super.getFlags();
        if (isFinal())
            flags.add("Final");
        if (isLiveliness())
            flags.add("Liveliness");
        if (isGroupInfo())
            flags.add("GroupInfo");
        return flags;
    }

    /**
     * Indicates whether the Reader is required to respond to the Heartbeat or if it
     * is just an advisory heartbeat. If set means the Writer does not require a
     * response from the Reader.
     */
    public boolean isFinal() {
        return (getFlagsInternal() & 2) != 0;
    }

    /**
     * Indicates that the DDS DataWriter associated with the RTPS Writer of the
     * message has manually asserted its LIVELINESS.
     */
    public boolean isLiveliness() {
        return (getFlagsInternal() & 4) != 0;
    }

    /**
     * Indicates the presence of additional information about the group of writers
     * (Writer Group) the sender belongs to.
     */
    public boolean isGroupInfo() {
        return (getFlagsInternal() & 8) != 0;
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] {
                "readerId", readerId,
                "writerId", writerId,
                "firstSN", firstSN,
                "lastSN", lastSN,
                "count", count };
    }

}
