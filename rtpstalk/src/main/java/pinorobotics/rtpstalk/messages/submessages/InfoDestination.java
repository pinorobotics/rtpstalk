package pinorobotics.rtpstalk.messages.submessages;

import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.transport.io.LengthCalculator;

public class InfoDestination extends Submessage {

    /**
     * Provides the GuidPrefix that should be used to reconstruct the GUIDs of all
     * the RTPS Reader entities whose EntityIds appears in the Submessages that
     * follow
     */
    public GuidPrefix guidPrefix;

    public InfoDestination() {

    }

    public InfoDestination(GuidPrefix guidPrefix) {
        this.guidPrefix = guidPrefix;
        submessageHeader = new SubmessageHeader(SubmessageKind.Predefined.INFO_DST.getValue(),
                LengthCalculator.getInstance().getFixedLength(InfoDestination.class));
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] { "guidPrefix", guidPrefix };
    }
}
