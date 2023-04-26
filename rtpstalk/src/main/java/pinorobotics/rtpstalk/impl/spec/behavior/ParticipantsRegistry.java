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
package pinorobotics.rtpstalk.impl.spec.behavior;

import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;

/**
 * One of the reasons for this class is that not all RTPS vendors include {@link
 * ParameterId#PID_PARTICIPANT_GUID} and {@link ParameterId#PID_DEFAULT_UNICAST_LOCATOR} (example
 * {@link VendorId.Predefined#CYCLONEDDS}) to SEDP data. When this happens we fallback to finding
 * this data from ParticipantData available from SPDP.
 *
 * <p>Must be thread-safe since new participants may be added from both SPDP and SEDP.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ParticipantsRegistry {

    private XLogger logger;
    private Map<Guid, ParameterList> participants = new ConcurrentHashMap<>();

    public ParticipantsRegistry(TracingToken tracingToken) {
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    public Optional<ParameterList> getSpdpDiscoveredParticipantData(
            GuidPrefix participantGuidPrefix) {
        var guid =
                new Guid(
                        participantGuidPrefix, EntityId.Predefined.ENTITYID_PARTICIPANT.getValue());
        return Optional.ofNullable(participants.get(guid));
    }

    public void remove(Guid participantGuid) {
        logger.fine("Removing participant {0} from the registry", participantGuid);
        participants.remove(participantGuid);
    }

    public void add(Guid participantGuid, ParameterList pl) {
        logger.fine("Adding new participant {0} to the registry", participantGuid);
        participants.put(participantGuid, pl);
    }
}
