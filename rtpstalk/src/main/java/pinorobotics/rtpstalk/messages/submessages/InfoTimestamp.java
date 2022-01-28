package pinorobotics.rtpstalk.messages.submessages;

import java.util.List;
import pinorobotics.rtpstalk.messages.submessages.elements.Timestamp;
import pinorobotics.rtpstalk.transport.io.LengthCalculator;

public class InfoTimestamp extends Submessage {

    public Timestamp timestamp;

    public InfoTimestamp() {

    }

    public InfoTimestamp(Timestamp timestamp) {
        submessageHeader = new SubmessageHeader(SubmessageKind.Predefined.INFO_TS.getValue(), 1,
                LengthCalculator.getInstance().getFixedLength(getClass()));
        this.timestamp = timestamp;
    }

    public List<String> getFlags() {
        var flags = super.getFlags();
        if (isInvalidate())
            flags.add("InvalidateFlag");
        return flags;
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] { "timestamp", timestamp };
    }

    /**
     * Subsequent Submessages should not be considered to have a valid timestamp.
     */
    private boolean isInvalidate() {
        return (getFlagsInternal() & 2) != 0;
    }

    public static InfoTimestamp now() {
        return new InfoTimestamp(Timestamp.now());
    }

}
