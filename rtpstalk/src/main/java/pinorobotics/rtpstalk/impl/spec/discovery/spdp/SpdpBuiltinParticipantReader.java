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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.RtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.KeyHash;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.StatusInfo;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.structure.history.CacheChange;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SpdpBuiltinParticipantReader extends RtpsReader<RtpsTalkParameterListMessage> {

    private Map<Guid, ParameterList> participants = new HashMap<>();
    private OperatingEntities operatingEntities;

    public SpdpBuiltinParticipantReader(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            byte[] guidPrefix,
            OperatingEntities operatingEntities) {
        super(
                config,
                tracingToken,
                new Guid(
                        guidPrefix,
                        EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue()),
                ReliabilityQosPolicy.Kind.BEST_EFFORT);
        this.operatingEntities = operatingEntities;
    }

    @Override
    protected boolean addChange(CacheChange<RtpsTalkParameterListMessage> cacheChange) {
        if (!super.addChange(cacheChange)) return false;
        var parameterList = cacheChange.getDataValue().parameterList();
        if (parameterList.getParameters().get(ParameterId.PID_PARTICIPANT_GUID)
                instanceof Guid guid) {
            participants.put(guid, parameterList);
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

    @Override
    protected void processInlineQos(Guid writer, ParameterList inlineQos) {
        var params = inlineQos.getParameters();
        if (params.isEmpty()) return;
        logger.fine("Processing inlineQos");
        processStatusInfo(writer, params);
    }

    private void processStatusInfo(Guid writerGuid, Map<ParameterId, ?> params) {
        if (params.get(ParameterId.PID_STATUS_INFO) instanceof StatusInfo info) {
            if (info.isDisposed()) {
                if (params.get(ParameterId.PID_KEY_HASH) instanceof KeyHash keyHash) {
                    var guid = keyHash.asGuid();
                    if (EntityId.Predefined.ENTITYID_PARTICIPANT.getValue().equals(guid.entityId)) {
                        logger.fine("Writer marked participant {0} as disposed", guid);
                        var writersToReaders =
                                Map.of(
                                        EntityId.Predefined
                                                .ENTITYID_SEDP_BUILTIN_PUBLICATIONS_ANNOUNCER
                                                .getValue(),
                                        EntityId.Predefined
                                                .ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR
                                                .getValue(),
                                        EntityId.Predefined
                                                .ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER
                                                .getValue(),
                                        EntityId.Predefined
                                                .ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR
                                                .getValue());
                        for (var pair : writersToReaders.entrySet()) {
                            operatingEntities
                                    .getWriters()
                                    .find(pair.getKey())
                                    .ifPresent(
                                            writer ->
                                                    writer.matchedReaderRemove(
                                                            new Guid(
                                                                    guid.guidPrefix,
                                                                    pair.getValue())));
                        }
                    }
                }
            }
        }
    }
}
