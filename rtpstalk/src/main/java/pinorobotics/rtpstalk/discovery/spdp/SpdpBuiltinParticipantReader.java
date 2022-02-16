package pinorobotics.rtpstalk.discovery.spdp;

import pinorobotics.rtpstalk.behavior.reader.RtpsReader;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;

public class SpdpBuiltinParticipantReader extends RtpsReader<ParameterList> {

    public SpdpBuiltinParticipantReader(GuidPrefix guidPrefix) {
        super(new Guid(guidPrefix, EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue()));
    }

}
