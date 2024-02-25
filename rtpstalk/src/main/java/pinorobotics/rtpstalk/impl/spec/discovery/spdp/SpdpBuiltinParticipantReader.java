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
package pinorobotics.rtpstalk.impl.spec.discovery.spdp;

import id.xfunction.logging.TracingToken;
import java.util.concurrent.Executor;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.ParticipantsRegistry;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.RtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.KeyHash;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.structure.history.CacheChange;

/**
 *
 *
 * <h2>Thread safe</h2>
 *
 * Since we run one SPDP reader per each network interface and they all bind to same multicast
 * address it means they may receive same data and run concurrently.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SpdpBuiltinParticipantReader extends RtpsReader<RtpsTalkParameterListMessage> {
    private LocalOperatingEntities operatingEntities;
    private ParticipantsRegistry participantsRegistry;

    public SpdpBuiltinParticipantReader(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            byte[] guidPrefix,
            LocalOperatingEntities operatingEntities,
            ParticipantsRegistry participantsRegistry) {
        super(
                config,
                tracingToken,
                RtpsTalkParameterListMessage.class,
                publisherExecutor,
                new Guid(
                        guidPrefix,
                        EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue()),
                ReliabilityQosPolicy.Kind.BEST_EFFORT);
        this.operatingEntities = operatingEntities;
        this.participantsRegistry = participantsRegistry;
    }

    @Override
    protected boolean addChange(CacheChange<RtpsTalkParameterListMessage> cacheChange) {
        var isAdded = super.addChange(cacheChange);
        if (!isAdded) return false;
        var parameterList = cacheChange.getDataValue().parameterList();
        parameterList.ifPresent(
                pl -> {
                    pl.getProtocolParameters()
                            .getFirstParameter(ParameterId.PID_PARTICIPANT_GUID, Guid.class)
                            .ifPresent(guid -> participantsRegistry.add(guid, pl));
                });
        return true;
    }

    @Override
    protected void processInlineQos(
            Guid writer, RtpsTalkParameterListMessage message, ParameterList inlineQos) {
        var inlineQosParams = inlineQos.getProtocolParameters();
        if (inlineQosParams.isEmpty()) return;
        logger.fine("Processing inlineQos");
        if (!inlineQosParams.hasDisposedObjects()) return;
        inlineQosParams
                .getFirstParameter(ParameterId.PID_KEY_HASH, KeyHash.class)
                .map(KeyHash::asGuid)
                .or(
                        () ->
                                message.parameterList()
                                        .map(ParameterList::getProtocolParameters)
                                        .flatMap(
                                                params ->
                                                        params.getFirstParameter(
                                                                ParameterId.PID_PARTICIPANT_GUID,
                                                                Guid.class)))
                .ifPresent(this::removeParticipant);
    }

    private void removeParticipant(Guid participantGuid) {
        if (EntityId.Predefined.ENTITYID_PARTICIPANT.getValue().equals(participantGuid.entityId)) {
            logger.fine("Writer marked participant {0} as disposed", participantGuid);
            for (var reader : operatingEntities.getLocalReaders().getEntities()) {
                reader.matchedWritersRemove(participantGuid.guidPrefix);
            }
            for (var writer : operatingEntities.getLocalWriters().getEntities()) {
                writer.matchedReadersRemove(participantGuid.guidPrefix);
            }
        }
        participantsRegistry.remove(participantGuid);
    }
}
