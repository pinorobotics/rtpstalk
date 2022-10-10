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

import id.xfunction.Preconditions;
import id.xfunction.logging.XLogger;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
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

    public Optional<D> extractMessage(Class<D> type, Data d) {
        var serializedPayload = d.serializedPayload.orElse(null);
        if (serializedPayload == null) {
            return d.inlineQos.map(
                    inlineQos -> {
                        RtpsTalkMessage message = null;
                        if (type == RtpsTalkParameterListMessage.class)
                            message = RtpsTalkParameterListMessage.withInlineQosOnly(inlineQos);
                        else if (type == RtpsTalkDataMessage.class)
                            message =
                                    new RtpsTalkDataMessage(
                                            new Parameters(inlineQos.getUserParameters()));
                        else
                            throw new IllegalArgumentException(
                                    "Type " + type.getName() + " is not supported");
                        return (D) message;
                    });
        }
        var serializedPayloadHeader = serializedPayload.serializedPayloadHeader.orElse(null);
        if (serializedPayloadHeader == null) {
            LOGGER.warning(
                    "Cannot extract data submessage when serializedPayloadHeader is empty, ignoring"
                            + " it...");
            return Optional.empty();
        }
        var representationId = serializedPayloadHeader.representation_identifier;
        var representation = representationId.getPredefinedValue().orElse(null);
        if (representation == null) {
            LOGGER.warning(
                    "Cannot extract data submessage with unknown representation identifier {0},"
                            + " ignoring it...",
                    serializedPayloadHeader.representation_identifier);
            return Optional.empty();
        }
        var inlineQos = d.inlineQos;
        switch (representation) {
            case CDR_LE:
                Preconditions.equals(type, RtpsTalkDataMessage.class, "Data message type mismatch");
                return Optional.of(
                        (D)
                                new RtpsTalkDataMessage(
                                        inlineQos
                                                .map(ParameterList::getUserParameters)
                                                .map(Parameters::new),
                                        ((RawData) serializedPayload.payload).getData()));
            case PL_CDR_LE:
                Preconditions.equals(
                        type, RtpsTalkParameterListMessage.class, "Data message type mismatch");
                return Optional.of(
                        (D)
                                new RtpsTalkParameterListMessage(
                                        inlineQos,
                                        Optional.of((ParameterList) serializedPayload.payload)));
            default:
                {
                    LOGGER.warning(
                            "Cannot extract data submessage with unsupported representation"
                                    + " identifier {0}, ignoring it...",
                            representationId);
                    return Optional.empty();
                }
        }
    }

    public Data packMessage(
            EntityId readerEntiyId, EntityId writerEntityId, Long seqNum, RtpsTalkMessage message) {
        var inlineQos = message.userInlineQos().map(v -> new ParameterList(v.getParameters()));
        var payload = Optional.<SerializedPayload>empty();
        if (message instanceof RtpsTalkDataMessage data)
            payload = data.data().map(RawData::new).map(d -> new SerializedPayload(d, true));
        else if (message instanceof RtpsTalkParameterListMessage params) {
            payload = params.parameterList().map(d -> new SerializedPayload(d, true));
            inlineQos = params.inlineQos();
        } else
            throw new UnsupportedOperationException(
                    "Cannot package message of type " + message.getClass().getSimpleName());
        return new Data(
                readerEntiyId, writerEntityId, new SequenceNumber(seqNum), inlineQos, payload);
    }
}
