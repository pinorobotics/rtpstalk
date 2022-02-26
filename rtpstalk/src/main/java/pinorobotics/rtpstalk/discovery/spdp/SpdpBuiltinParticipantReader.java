/*
 * Copyright 2022 rtpstalk project
 * 
 * Website: https://github.com/pinorobotics/rtpstalk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/** @author aeon_flux aeon_flux@eclipso.ch */
public class SpdpBuiltinParticipantReader extends RtpsReader<ParameterList> {

    private Map<Guid, ParameterList> participants = new HashMap<>();

    public SpdpBuiltinParticipantReader(GuidPrefix guidPrefix) {
        super(
                new Guid(
                        guidPrefix,
                        EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue()));
    }

    @Override
    protected boolean addChange(CacheChange<ParameterList> cacheChange) {
        if (!super.addChange(cacheChange)) return false;
        if (cacheChange.getDataValue().params.get(ParameterId.PID_PARTICIPANT_GUID)
                instanceof Guid guid) {
            participants.put(guid, cacheChange.getDataValue());
        }
        return true;
    }

    public Optional<ParameterList> getSpdpDiscoveredParticipantData(
            GuidPrefix participantGuidPrefix) {
        var guid =
                new Guid(
                        participantGuidPrefix, EntityId.Predefined.ENTITYID_PARTICIPANT.getValue());
        return Optional.ofNullable(participants.get(guid));
    }
}
