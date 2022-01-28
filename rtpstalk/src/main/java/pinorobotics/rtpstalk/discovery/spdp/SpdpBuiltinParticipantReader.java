package pinorobotics.rtpstalk.discovery.spdp;

import java.nio.channels.DatagramChannel;
import pinorobotics.rtpstalk.behavior.reader.RtpsReader;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;

public class SpdpBuiltinParticipantReader extends RtpsReader {

    public SpdpBuiltinParticipantReader(GuidPrefix guidPrefix, DatagramChannel dc, int packetBufferSize) {
        super(new Guid(guidPrefix, EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue()),
                dc, packetBufferSize);
    }

}
