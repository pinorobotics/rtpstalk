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

import java.util.List;
import java.util.Optional;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class Data extends Submessage {

    public short extraFlags;

    /**
     * Contains the number of octets starting from the first octet immediately following this field
     * until the first octet of the inlineQos SubmessageElement. If the inlineQos SubmessageElement
     * is not present (i.e., the InlineQosFlag is not set), then octetsToInlineQos contains the
     * offset to the next field after the inlineQos.
     */
    public short octetsToInlineQos;

    /** Identifies the RTPS Reader entity that is being informed of the change to the data-object */
    public EntityId readerId;

    /** Identifies the RTPS Writer entity that made the change to the data-object */
    public EntityId writerId;

    /**
     * Uniquely identifies the change and the relative order for all changes made by the RTPS Writer
     * identified by the writerGuid. Each change gets a consecutive sequence number. Each RTPS
     * Writer maintains is own sequence number
     */
    public SequenceNumber writerSN;

    /**
     * Present only if the InlineQosFlag is set in the header. Contains QoS that may affect the
     * interpretation of the message
     */
    public transient ParameterList inlineQos = new ParameterList();

    public transient Optional<SerializedPayload> serializedPayload = Optional.empty();

    public Data() {}

    public Data(
            EntityId.Predefined readerId,
            EntityId.Predefined writerId,
            SequenceNumber writerSN,
            SerializedPayload serializedPayload) {
        this(readerId.getValue(), writerId.getValue(), writerSN, serializedPayload);
    }

    public Data(
            EntityId.Predefined readerId,
            EntityId.Predefined writerId,
            SequenceNumber writerSN,
            ParameterList inlineQos,
            SerializedPayload serializedPayload) {
        this(readerId.getValue(), writerId.getValue(), writerSN, inlineQos, serializedPayload);
    }

    public Data(
            EntityId.Predefined readerId,
            EntityId.Predefined writerId,
            SequenceNumber writerSN,
            ParameterList inlineQos) {
        this(readerId.getValue(), writerId.getValue(), writerSN, inlineQos, Optional.empty());
    }

    public Data(
            EntityId readerId,
            EntityId writerId,
            SequenceNumber writerSN,
            SerializedPayload serializedPayload) {
        this(readerId, writerId, writerSN, new ParameterList(), serializedPayload);
    }

    public Data(
            EntityId readerId,
            EntityId writerId,
            SequenceNumber writerSN,
            ParameterList inlineQos,
            SerializedPayload serializedPayload) {
        this(readerId, writerId, writerSN, inlineQos, Optional.of(serializedPayload));
    }

    public Data(
            EntityId readerId,
            EntityId writerId,
            SequenceNumber writerSN,
            ParameterList inlineQos,
            Optional<SerializedPayload> serializedPayload) {
        this.octetsToInlineQos =
                (short)
                        (LengthCalculator.getInstance().getFixedLength(EntityId.class) * 2
                                + LengthCalculator.getInstance()
                                        .getFixedLength(SequenceNumber.class));
        this.readerId = readerId;
        this.writerId = writerId;
        this.writerSN = writerSN;
        this.inlineQos = inlineQos;
        this.serializedPayload = serializedPayload;
        var flags = RtpsTalkConfiguration.ENDIANESS_BIT;
        if (serializedPayload.isPresent()) flags |= 0b100;
        if (!inlineQos.isEmpty()) flags |= 2;
        submessageHeader =
                new SubmessageHeader(
                        SubmessageKind.Predefined.DATA.getValue(),
                        flags,
                        LengthCalculator.getInstance().calculateLength(this));
    }

    @Override
    public List<String> getFlags() {
        var flags = super.getFlags();
        if (isInlineQos()) flags.add("InlineQos");
        if (isData()) flags.add("Data");
        if (isKey()) flags.add("Key");
        if (isNonStandardPayload()) flags.add("NonStandardPayload");
        return flags;
    }

    public boolean isInlineQos() {
        return (getFlagsInternal() & 2) != 0;
    }

    public boolean isData() {
        return (getFlagsInternal() & 4) != 0;
    }

    public boolean isKey() {
        return (getFlagsInternal() & 8) != 0;
    }

    public boolean isNonStandardPayload() {
        return (getFlagsInternal() & 10) != 0;
    }

    public Optional<SerializedPayload> getSerializedPayload() {
        return serializedPayload;
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] {
            "extraFlags", extraFlags,
            "octetsToInlineQos", octetsToInlineQos,
            "readerId", readerId,
            "writerId", writerId,
            "writerSN", writerSN,
            "inlineQos", inlineQos,
            "serializedPayload", serializedPayload.map(Object::toString).orElse("null")
        };
    }
}
