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
package pinorobotics.rtpstalk.impl.spec.messages.submessages;

import id.xfunction.XJsonStringBuilder;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RepresentationIdentifier.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SubmessageElement;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SerializedPayload implements SubmessageElement {

    /**
     * SerializedPayloadHeader can be missing if the payload is part of {@link DataFrag} submessage
     */
    public transient Optional<SerializedPayloadHeader> serializedPayloadHeader = Optional.empty();

    public Payload payload;

    public SerializedPayload() {}

    public SerializedPayload(Payload payload, boolean withHeader) {
        this.payload = payload;
        if (withHeader)
            this.serializedPayloadHeader =
                    Optional.of(getPredefinedPayloadHeader(payload.getRepresentationIdentifier()));
    }

    public SerializedPayload(SerializedPayloadHeader payloadHeader, Payload payload) {
        this.serializedPayloadHeader = Optional.of(payloadHeader);
        this.payload = payload;
    }

    private static SerializedPayloadHeader getPredefinedPayloadHeader(
            Predefined representationIdentifier) {
        return switch (representationIdentifier) {
            case PL_CDR_LE -> SerializedPayloadHeader.DEFAULT_PARAMETER_LIST_HEADER;
            case CDR_LE -> SerializedPayloadHeader.DEFAULT_DATA_HEADER;
            default -> throw new UnsupportedOperationException(
                    "Unsupported representation identifier " + representationIdentifier);
        };
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("serializedPayloadHeader", serializedPayloadHeader);
        builder.append("payload", payload);
        return builder.toString();
    }

    public Payload getPayload() {
        return payload;
    }
}
