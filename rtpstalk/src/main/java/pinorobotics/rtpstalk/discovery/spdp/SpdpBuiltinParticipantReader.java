package pinorobotics.rtpstalk.discovery.spdp;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import pinorobotics.rtpstalk.behavior.reader.RtpsReader;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.structure.CacheChange;

public class SpdpBuiltinParticipantReader extends RtpsReader<ParameterList> {

    private Map<Guid, ParameterList> participants = new HashMap<>();

    public SpdpBuiltinParticipantReader(GuidPrefix guidPrefix) {
        super(new Guid(guidPrefix, EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue()));
    }

    @Override
    protected boolean addChange(CacheChange<ParameterList> cacheChange) {
        if (!super.addChange(cacheChange))
            return false;
        if (cacheChange.getDataValue().params.get(ParameterId.PID_PARTICIPANT_GUID) instanceof Guid guid) {
            participants.put(guid, cacheChange.getDataValue());
        }
        return true;
    }

    public Optional<ParameterList> getSpdpDiscoveredParticipantData(GuidPrefix participantGuidPrefix) {
        var guid = new Guid(participantGuidPrefix, EntityId.Predefined.ENTITYID_PARTICIPANT.getValue());
        return Optional.ofNullable(participants.get(guid));
    }
}
