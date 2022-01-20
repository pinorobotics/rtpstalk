package pinorobotics.rtpstalk.dto.submessages;

import java.util.List;
import pinorobotics.rtpstalk.dto.submessages.elements.Count;
import pinorobotics.rtpstalk.dto.submessages.elements.EntityId;
import pinorobotics.rtpstalk.dto.submessages.elements.SequenceNumberSet;

public class AckNack extends Submessage {

    /**
     * Identifies the Reader entity that acknowledges receipt of certain sequence
     * numbers and/or requests to receive certain sequence numbers.
     */
    public EntityId readerId;

    /**
     * Identifies the Writer entity that is the target of the AckNack message. This
     * is the Writer Entity that is being asked to re-send some sequence numbers or
     * is being informed of the reception of certain sequence numbers.
     */
    public EntityId writerId;

    /**
     * Communicates the state of the reader to the writer. All sequence numbers up
     * to the one prior to readerSNState.base are confirmed as received by the
     * reader. The sequence numbers that appear in the set indicate missing sequence
     * numbers on the reader side. The ones that do not appear in the set are
     * undetermined (could be received or not).
     */
    public SequenceNumberSet readerSNState;

    /**
     * A counter that is incremented each time a new AckNack message is sent.
     * Provides the means for a Writer to detect duplicate AckNack messages that can
     * result from the presence of redundant communication paths.
     */
    public Count count;

    public AckNack() {

    }

    @Override
    public List<String> getFlags() {
        var flags = super.getFlags();
        if (isFinal())
            flags.add("Final");
        return flags;
    }

    /**
     * False indicates whether writer must respond to the AckNack message with a
     * Heartbeat message
     */
    public boolean isFinal() {
        return (getFlagsInternal() & 2) != 0;
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] {
                "readerId", readerId,
                "writerId", writerId,
                "readerSNState", readerSNState,
                "count", count };
    }

}
