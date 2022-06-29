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
package pinorobotics.rtpstalk.impl;

import id.xfunction.logging.XLogger;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Payload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsDataPackager<D extends RtpsTalkMessage> {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsDataPackager.class);

    public Optional<D> extractMessage(Data d) {
        var representationId =
                d.serializedPayload.serializedPayloadHeader.representation_identifier;
        var representationOpt =
                d.serializedPayload.serializedPayloadHeader.representation_identifier
                        .findPredefined();
        if (representationOpt.isEmpty()) {
            LOGGER.warning(
                    "Received data submessage with unknown representation identifier {0}, ignoring"
                            + " it...",
                    representationId);
            return Optional.empty();
        }
        var inlineQos = new Parameters(d.inlineQos.getUserParameters());
        switch (representationOpt.get()) {
            case CDR_LE:
                return Optional.of(
                        (D)
                                new RtpsTalkDataMessage(
                                        inlineQos, ((RawData) d.serializedPayload.payload).data));
            case PL_CDR_LE:
                return Optional.of(
                        (D)
                                new RtpsTalkParameterListMessage(
                                        inlineQos, (ParameterList) d.serializedPayload.payload));
            default:
                {
                    LOGGER.warning(
                            "Received data submessage with unsupported representation identifier"
                                    + " {0}, ignoring it...",
                            representationId);
                    return Optional.empty();
                }
        }
    }

    public Data packMessage(
            EntityId readerEntiyId, EntityId writerEntityId, Long seqNum, RtpsTalkMessage message) {
        var inlineQos = new ParameterList(message.inlineQos().getParameters());
        Payload payload = null;
        if (message instanceof RtpsTalkDataMessage data) payload = new RawData(data.data());
        else if (message instanceof RtpsTalkParameterListMessage params)
            payload = params.parameterList();
        else
            throw new UnsupportedOperationException(
                    "Cannot package message of type " + message.getClass().getSimpleName());
        return new Data(
                readerEntiyId,
                writerEntityId,
                new SequenceNumber(seqNum),
                inlineQos,
                new SerializedPayload(payload));
    }
}
