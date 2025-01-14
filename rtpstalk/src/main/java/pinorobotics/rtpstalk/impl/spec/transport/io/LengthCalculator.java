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
package pinorobotics.rtpstalk.impl.spec.transport.io;

import id.xfunction.Preconditions;
import id.xfunction.lang.XRE;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointQos;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.impl.spec.messages.ByteSequence;
import pinorobotics.rtpstalk.impl.spec.messages.DataRepresentationQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.DeadlineQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.DestinationOrderQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityServiceQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.DurationT;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Header;
import pinorobotics.rtpstalk.impl.spec.messages.HistoryQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.IntSequence;
import pinorobotics.rtpstalk.impl.spec.messages.KeyHash;
import pinorobotics.rtpstalk.impl.spec.messages.LatencyBudgetQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.LifespanQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.LocatorKind;
import pinorobotics.rtpstalk.impl.spec.messages.ProtocolId;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.ShortSequence;
import pinorobotics.rtpstalk.impl.spec.messages.StatusInfo;
import pinorobotics.rtpstalk.impl.spec.messages.UnsignedInt;
import pinorobotics.rtpstalk.impl.spec.messages.UserDataQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.AckNack;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Gap;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RepresentationIdentifier;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SubmessageHeader;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SubmessageKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Timestamp;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class LengthCalculator {

    private static LengthCalculator calculator = new LengthCalculator();
    public static final int ADDRESS_SIZE = 16;

    public static LengthCalculator getInstance() {
        return calculator;
    }

    public int getFixedLength(Class<?> clazz) {
        var len = getFixedLengthInternal(clazz);
        if (len == -1) throw new XRE("Fixed length is unknown for type %s", clazz);
        return len;
    }

    // TODO add all to HashMap to avoid recalculations
    public int getFixedLengthInternal(Class<?> clazz) {
        if (clazz == EntityId.class) return EntityId.SIZE;
        if (clazz == UnsignedInt.class) return Integer.BYTES;
        if (clazz == BuiltinEndpointSet.class) return Integer.BYTES;
        if (clazz == SequenceNumber.class) return SequenceNumber.SIZE;
        if (clazz == Timestamp.class) return Integer.BYTES * 2;
        if (clazz == ParameterId.class) return Short.BYTES;
        if (clazz == DurationT.class) return Integer.BYTES * 2;
        if (clazz == LocatorKind.class) return Integer.BYTES;
        if (clazz == GuidPrefix.class) return GuidPrefix.SIZE;
        if (clazz == ProtocolVersion.class) return 2;
        if (clazz == VendorId.class) return 2;
        if (clazz == InfoTimestamp.class) return getFixedLength(Timestamp.class);
        if (clazz == InfoDestination.class) return InfoDestination.SIZE;
        if (clazz == RepresentationIdentifier.class) return RepresentationIdentifier.SIZE;
        if (clazz == SerializedPayloadHeader.class) return SerializedPayloadHeader.SIZE;
        if (clazz == SubmessageKind.class) return 1;
        if (clazz == SubmessageHeader.class)
            return getFixedLength(SubmessageKind.class) + 1 + Short.BYTES;
        if (clazz == Locator.class)
            return getFixedLength(LocatorKind.class) + Integer.BYTES + ADDRESS_SIZE;
        if (clazz == Guid.class)
            return getFixedLength(GuidPrefix.class) + getFixedLength(EntityId.class);
        if (clazz == Count.class) return Integer.BYTES;
        if (clazz == BuiltinEndpointQos.class) return Integer.BYTES;
        if (clazz == ReliabilityQosPolicy.class)
            return Integer.BYTES + getFixedLength(DurationT.class);
        if (clazz == DestinationOrderQosPolicy.class) return Integer.BYTES;
        if (clazz == DurabilityQosPolicy.class) return Integer.BYTES;
        if (clazz == DeadlineQosPolicy.class) return getFixedLengthInternal(DurationT.class);
        if (clazz == LatencyBudgetQosPolicy.class) return getFixedLengthInternal(DurationT.class);
        if (clazz == LifespanQosPolicy.class) return getFixedLengthInternal(DurationT.class);
        if (clazz == DurabilityServiceQosPolicy.class)
            return getFixedLength(DurationT.class)
                    + getFixedLength(HistoryQosPolicy.class)
                    + Integer.BYTES * 4;
        if (clazz == HistoryQosPolicy.class) return 2 * Integer.BYTES;
        if (clazz == KeyHash.class) return KeyHash.SIZE;
        if (clazz == Heartbeat.class)
            return getFixedLength(EntityId.class) * 2
                    + getFixedLength(SequenceNumber.class) * 2
                    + getFixedLength(Count.class);
        if (clazz == StatusInfo.class) return StatusInfo.SIZE;
        if (clazz == ProtocolId.class) return ProtocolId.SIZE;
        if (clazz == Header.class)
            return getFixedLength(ProtocolId.class)
                    + getFixedLength(ProtocolVersion.class)
                    + getFixedLength(VendorId.class)
                    + getFixedLength(GuidPrefix.class);
        return -1;
    }

    public int calculateLength(Object obj) {
        var len = getFixedLengthInternal(obj.getClass());
        if (len != -1) return len;
        if (obj instanceof Data d)
            return calculateSubmessagePadding(
                    Short.BYTES * 2
                            + getFixedLength(EntityId.class) * 2
                            + getFixedLength(SequenceNumber.class)
                            + calculateLength(d.inlineQos)
                            + calculateLength(d.serializedPayload));
        if (obj instanceof DataFrag d)
            return calculateSubmessagePadding(
                    Short.BYTES * 4
                            + getFixedLength(EntityId.class) * 2
                            + getFixedLength(SequenceNumber.class)
                            + Integer.BYTES * 2
                            + calculateLength(d.inlineQos)
                            + calculateLength(d.serializedPayload));
        if (obj instanceof SerializedPayload p)
            return calculateLength(p.serializedPayloadHeader) + calculateLength(p.payload);
        if (obj instanceof ParameterList pl) return calculateParameterListLength(pl);
        if (obj instanceof String s) return s.length() + 1 + Integer.BYTES;
        if (obj instanceof UserDataQosPolicy policy) return calculateLength(policy.value);
        if (obj instanceof DataRepresentationQosPolicy policy) return calculateLength(policy.value);
        if (obj instanceof AckNack ackNack) {
            return getFixedLength(EntityId.class) * 2
                    + calculateLength(ackNack.readerSNState)
                    + getFixedLength(Count.class);
        }
        if (obj instanceof Gap gap)
            return getFixedLength(EntityId.class) * 2
                    + getFixedLength(SequenceNumber.class)
                    + calculateLength(gap.gapList)
                    + calculateLength(gap.gapStartGSN)
                    + calculateLength(gap.gapEndGSN);
        if (obj instanceof SequenceNumberSet set)
            return getFixedLength(SequenceNumber.class)
                    + Integer.BYTES
                    + Integer.BYTES * set.bitmap.length;
        if (obj instanceof ByteSequence seq) return Integer.BYTES + seq.length;
        if (obj instanceof IntSequence intSeq)
            return Integer.BYTES + Integer.BYTES * intSeq.data.length;
        if (obj instanceof ShortSequence shortSeq)
            return Integer.BYTES + Short.BYTES * shortSeq.data.length;
        if (obj instanceof RawData rawData) return rawData.getData().length;
        if (obj instanceof Optional<?> opt) return opt.isEmpty() ? 0 : calculateLength(opt.get());
        throw new XRE("Cannot calculate length for an object of type %s", obj.getClass().getName());
    }

    private int calculateParameterLength(Entry<ParameterId, List<Object>> param) {
        return getFixedLength(ParameterId.class) * param.getValue().size()
                + Short.BYTES /* length */
                + calculateParameterValueLength(param);
    }

    private int calculateLength(List<Object> values) {
        return values.stream().mapToInt(this::calculateLength).sum();
    }

    public int calculateParameterValueLength(Entry<ParameterId, List<Object>> param) {
        ParameterId id = param.getKey();
        Preconditions.notNull(param.getValue());
        var values = param.getValue();
        if (values.isEmpty()) return 0;
        var len =
                switch (id) {
                    case PID_ENTITY_NAME -> calculateLength(values);
                    case PID_TOPIC_NAME -> calculateLength(values);
                    case PID_TYPE_NAME -> calculateLength(values);
                    case PID_SENTINEL -> 0;
                    case PID_USER_DATA -> calculateLength(values);
                    case PID_DATA_REPRESENTATION -> calculateLength(values);
                    case PID_KEY_HASH,
                                    PID_DOMAIN_ID,
                                    PID_BUILTIN_ENDPOINT_SET,
                                    PID_PARTICIPANT_LEASE_DURATION,
                                    PID_DEFAULT_UNICAST_LOCATOR,
                                    PID_METATRAFFIC_UNICAST_LOCATOR,
                                    PID_UNICAST_LOCATOR,
                                    PID_PARTICIPANT_GUID,
                                    PID_ENDPOINT_GUID,
                                    PID_PROTOCOL_VERSION,
                                    PID_VENDORID,
                                    PID_BUILTIN_ENDPOINT_QOS,
                                    PID_LATENCY_BUDGET,
                                    PID_LIFESPAN,
                                    PID_RELIABILITY,
                                    PID_DURABILITY,
                                    PID_DURABILITY_SERVICE,
                                    PID_STATUS_INFO,
                                    PID_DEADLINE,
                                    PID_HISTORY,
                                    PID_DESTINATION_ORDER ->
                            getFixedLength(id.getParameterClass());
                    default ->
                            throw new XRE(
                                    "Cannot calculate length for an unknown parameter id %s", id);
                };

        return calculateParameterListValuePadding(len);
    }

    @RtpsSpecReference(
            paragraph = "9.4.2.11",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "ParameterList A ParameterList contains a list of Parameters, terminated"
                            + " with a sentinel. Each Parameter within the ParameterList starts"
                            + " aligned on a 4-byte boundary with respect to the start of the"
                            + " ParameterList.")
    private int calculateParameterListValuePadding(int len) {
        return len + InternalUtils.getInstance().padding(len, 4);
    }

    @RtpsSpecReference(
            paragraph = "9.4.1",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "The PSM aligns each Submessage on a 32-bit boundary with respect to the start"
                            + " of the Message")
    private int calculateSubmessagePadding(int lenInBytes) {
        return lenInBytes + InternalUtils.getInstance().padding(lenInBytes, 4);
    }

    private int calculateParameterListLength(ParameterList parameterList) {
        if (parameterList.isEmpty()) return 0;
        return calculateParameterLength(Map.entry(ParameterId.PID_SENTINEL, List.of(0)))
                + parameterList.getProtocolParameters().stream()
                        .mapToInt(this::calculateParameterLength)
                        .sum()
                + parameterList.getUserParameters().stream()
                        .mapToInt(this::calculateUserParameterListLength)
                        .sum();
    }

    private int calculateUserParameterListLength(Entry<Short, List<byte[]>> param) {
        return Short.BYTES * param.getValue().size() /* param id */
                + Short.BYTES /* length */
                + calculateUserParameterValueLength(param.getValue());
    }

    public int calculateUserParameterValueLength(List<byte[]> values) {
        var len = values.stream().mapToInt(a -> a.length).sum();
        return calculateParameterListValuePadding(len);
    }

    @RtpsSpecReference(
            paragraph = "9.4.1",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "The PSM aligns each Submessage on a 32-bit boundary with respect to the start"
                            + " of the Message")
    public void validateSubmessageSize(int submessageSizeInBytes) {
        Preconditions.isTrue(
                submessageSizeInBytes % 4 == 0,
                "submessageSizeInBytes must be aligned on 32-bit boundary: "
                        + submessageSizeInBytes);
    }
}
