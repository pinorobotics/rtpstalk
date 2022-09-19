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

import id.xfunction.Preconditions;
import java.util.List;
import java.util.Optional;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.UnsignedInt;
import pinorobotics.rtpstalk.impl.spec.messages.UnsignedShort;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataFrag extends Submessage implements DataSubmessage {

    /** Size of the DataFrag submessage excluding sizze of data fragment */
    public static final int EMPTY_SUBMESSAGE_SIZE = calcEmptySubmessageSize();

    public short extraFlags;

    /**
     * Contains the number of octets starting from the first octet immediately following this field
     * until the first octet of the inlineQos SubmessageElement. If the inlineQos SubmessageElement
     * is not present (i.e., the InlineQosFlag is not set), then octetsToInlineQos contains the
     * offset to the next field after the inlineQos.
     */
    public UnsignedShort octetsToInlineQos;

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
     * Indicates the starting fragment for the series of fragments in serializedData. Fragment
     * numbering starts with number 1
     */
    public UnsignedInt fragmentStartingNum;

    /**
     * The number of consecutive fragments contained in this Submessage, starting at
     * fragmentStartingNum.
     */
    public UnsignedShort fragmentsInSubmessage;

    /** The size of an individual fragment in bytes. The maximum fragment size equals 64K. */
    public UnsignedShort fragmentSize;

    /** The total size in bytes of the original data before fragmentation. */
    public int dataSize;

    /**
     * Present only if the InlineQosFlag is set in the header. Contains QoS that may affect the
     * interpretation of the message
     */
    public transient Optional<ParameterList> inlineQos = Optional.empty();

    public transient SerializedPayload serializedPayload;

    public DataFrag() {}

    public DataFrag(
            EntityId readerId,
            EntityId writerId,
            SequenceNumber writerSN,
            long fragmentStartingNum,
            int fragmentsInSubmessage,
            int fragmentSize,
            int dataSize,
            Optional<ParameterList> inlineQos,
            SerializedPayload serializedPayload) {
        this.octetsToInlineQos =
                new UnsignedShort(
                        Short.BYTES * 2
                                + Integer.BYTES * 2
                                + LengthCalculator.getInstance().getFixedLength(EntityId.class) * 2
                                + LengthCalculator.getInstance()
                                        .getFixedLength(SequenceNumber.class));
        this.readerId = readerId;
        this.writerId = writerId;
        this.writerSN = writerSN;
        this.fragmentStartingNum = new UnsignedInt(fragmentStartingNum);
        this.fragmentsInSubmessage = new UnsignedShort(fragmentsInSubmessage);
        this.fragmentSize = new UnsignedShort(fragmentSize);
        this.dataSize = dataSize;
        this.inlineQos = inlineQos;
        this.serializedPayload = serializedPayload;
        var flags = RtpsTalkConfiguration.ENDIANESS_BIT;
        if (!inlineQos.isEmpty()) flags |= 2;
        submessageHeader =
                new SubmessageHeader(
                        SubmessageKind.Predefined.DATA_FRAG.getValue(),
                        flags,
                        LengthCalculator.getInstance().calculateLength(this));
        validate();
    }

    @RtpsSpecReference(
            paragraph = "8.3.7.3",
            protocolVersion = Predefined.Version_2_3,
            text = "Validity")
    private void validate() {
        Preconditions.isTrue(writerSN.value >= 1, "writerSN must be greater than 0");
        Preconditions.isTrue(
                fragmentStartingNum.getUnsigned() >= 1,
                "fragmentStartingNum must be greater than 0");
    }

    @Override
    public List<String> getFlags() {
        var flags = super.getFlags();
        if (isInlineQos()) flags.add("InlineQos");
        if (isKey()) flags.add("Key");
        if (isNonStandardPayload()) flags.add("NonStandardPayload");
        return flags;
    }

    @Override
    public boolean isInlineQos() {
        return (getFlagsInternal() & 2) != 0;
    }

    public boolean isKey() {
        return (getFlagsInternal() & 4) != 0;
    }

    public boolean isNonStandardPayload() {
        return (getFlagsInternal() & 8) != 0;
    }

    @Override
    public Optional<SerializedPayload> getSerializedPayload() {
        return Optional.of(serializedPayload);
    }

    @Override
    public void setSerializedPayload(SerializedPayload serializedPayload) {
        this.serializedPayload = serializedPayload;
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] {
            "extraFlags", extraFlags,
            "readerId", readerId,
            "writerId", writerId,
            "writerSN", writerSN,
            "inlineQos", inlineQos,
            "fragmentStartingNum", fragmentStartingNum,
            "fragmentsInSubmessage", fragmentsInSubmessage,
            "fragmentSize", fragmentSize,
            "dataSize", dataSize,
            "serializedPayload", serializedPayload
        };
    }

    @Override
    public Optional<ParameterList> getInlineQos() {
        return inlineQos;
    }

    @Override
    public void setInlineQos(ParameterList parameterList) {
        if (submessageHeader != null) submessageHeader.submessageFlag |= 2;
        inlineQos = Optional.of(parameterList);
    }

    public static boolean hasSerializedPayloadHeader(long fragmentStartingNum) {
        return fragmentStartingNum == 1;
    }

    private static int calcEmptySubmessageSize() {
        var emptyFragment =
                new DataFrag(
                        new EntityId(),
                        new EntityId(),
                        new SequenceNumber(1),
                        1,
                        0,
                        0,
                        0,
                        Optional.empty(),
                        new SerializedPayload(
                                SerializedPayloadHeader.DEFAULT_DATA_HEADER,
                                new RawData(new byte[0])));
        return LengthCalculator.getInstance().calculateLength(emptyFragment);
    }
}
