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
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.DurationT;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
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

    private class Participant {

        private Guid guid;
        private ParameterList pl;
        private Instant lastLeaseTimestamp;

        public Participant(Guid guid, ParameterList pl) {
            this.guid = guid;
            this.pl = pl;
            this.lastLeaseTimestamp = Instant.now();
        }

        public Guid getGuid() {
            return guid;
        }

        public ParameterList getParameterList() {
            return pl;
        }

        void updateLeaseTimestamp() {
            this.lastLeaseTimestamp = Instant.now();
        }

        boolean isLeaseExpired() {
            return pl.getProtocolParameters()
                    .getFirstParameter(ParameterId.PID_PARTICIPANT_LEASE_DURATION, DurationT.class)
                    .map(DurationT::toDuration)
                    .map(
                            leaseDuration ->
                                    lastLeaseTimestamp.plus(leaseDuration).isBefore(Instant.now()))
                    .orElse(false);
        }
    }

    private XLogger logger;
    private Map<Guid, Participant> participants = new ConcurrentHashMap<>();
    private LocalOperatingEntities operatingEntities;

    public ParticipantsRegistry(
            TracingToken tracingToken, LocalOperatingEntities operatingEntities) {
        logger = XLogger.getLogger(getClass(), tracingToken);
        this.operatingEntities = operatingEntities;
    }

    public Optional<ParameterList> getSpdpDiscoveredParticipantData(
            GuidPrefix participantGuidPrefix) {
        var guid =
                new Guid(
                        participantGuidPrefix, EntityId.Predefined.ENTITYID_PARTICIPANT.getValue());
        return Optional.ofNullable(participants.get(guid)).map(Participant::getParameterList);
    }

    public void removeParticipant(Guid participantGuid) {
        logger.info("Removing participant {0} from the registry", participantGuid);
        if (EntityId.Predefined.ENTITYID_PARTICIPANT.getValue().equals(participantGuid.entityId)) {
            for (var reader : operatingEntities.getLocalReaders().getEntities()) {
                reader.matchedWritersRemove(participantGuid.guidPrefix);
            }
            for (var writer : operatingEntities.getLocalWriters().getEntities()) {
                writer.matchedReadersRemove(participantGuid.guidPrefix);
            }
        }
        participants.remove(participantGuid);
    }

    public void updateLease(Guid participantGuid) {
        logger.fine("Updating lease for participant {0}", participantGuid);
        var participant = participants.get(participantGuid);
        if (participant == null) {
            logger.fine("Participant {0} cound not be found inside the registry", participantGuid);
        }
        participant.updateLeaseTimestamp();
    }

    public void add(Guid participantGuid, ParameterList pl) {
        logger.info("Adding new participant {0} to the registry", participantGuid);
        participants.put(participantGuid, new Participant(participantGuid, pl));
    }

    @RtpsSpecReference(
            paragraph = "8.5.5.2",
            protocolVersion = Predefined.Version_2_3,
            text = "Removal of a previously discovered Participant")
    public void removeParticipantsWithExpiredLease() {
        logger.fine("Removing participants for which lease is expired");
        participants.values().stream()
                .filter(Participant::isLeaseExpired)
                .map(Participant::getGuid)
                .forEach(
                        participantGuid -> {
                            logger.fine("Lease is expired for participant {0}", participantGuid);
                            removeParticipant(participantGuid);
                        });
    }
}
