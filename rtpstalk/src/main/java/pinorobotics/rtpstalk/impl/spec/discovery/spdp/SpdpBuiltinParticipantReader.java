/*
 * Copyright 2022 pinorobotics
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
import pinorobotics.rtpstalk.impl.spec.behavior.ParticipantsRegistry;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatelessRtpsReader;
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
 * <p>Since we run one SPDP reader per each network interface and they all bind to same multicast
 * address it means they may receive same data and run concurrently.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SpdpBuiltinParticipantReader
        extends StatelessRtpsReader<RtpsTalkParameterListMessage> {
    private ParticipantsRegistry participantsRegistry;

    public SpdpBuiltinParticipantReader(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            ParticipantsRegistry participantsRegistry) {
        super(
                config,
                tracingToken,
                RtpsTalkParameterListMessage.class,
                publisherExecutor,
                EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue(),
                ReliabilityQosPolicy.Kind.BEST_EFFORT);
        this.participantsRegistry = participantsRegistry;
    }

    @Override
    protected boolean addChange(CacheChange<RtpsTalkParameterListMessage> cacheChange) {
        var isAdded = super.addChange(cacheChange);
        var pl = cacheChange.getDataValue().parameterList().orElse(null);
        if (pl == null) return isAdded;
        var params = pl.getProtocolParameters();
        params.getFirstParameter(ParameterId.PID_PARTICIPANT_GUID, Guid.class)
                .ifPresent(
                        guid -> {
                            if (isAdded) participantsRegistry.add(guid, pl);
                            else participantsRegistry.updateLease(guid);
                        });
        return isAdded;
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
                .ifPresent(
                        participantGuid -> {
                            logger.fine(
                                    "Writer marked participant {0} as disposed", participantGuid);
                            participantsRegistry.removeParticipant(participantGuid);
                        });
    }
}
