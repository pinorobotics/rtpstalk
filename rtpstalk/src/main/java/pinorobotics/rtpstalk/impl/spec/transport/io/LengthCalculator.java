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
package pinorobotics.rtpstalk.impl.spec.transport.io;

import id.xfunction.lang.XRE;
import java.util.Map;
import java.util.Map.Entry;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointQos;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.impl.spec.messages.ByteSequence;
import pinorobotics.rtpstalk.impl.spec.messages.DestinationOrderQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityServiceQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Duration;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.HistoryQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.IntSequence;
import pinorobotics.rtpstalk.impl.spec.messages.KeyHash;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.LocatorKind;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.UserDataQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.AckNack;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
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

/** @author aeon_flux aeon_flux@eclipso.ch */
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
        if (clazz == EntityId.class) return Integer.BYTES;
        if (clazz == BuiltinEndpointSet.class) return Integer.BYTES;
        if (clazz == SequenceNumber.class) return Integer.BYTES * 2;
        if (clazz == Timestamp.class) return Integer.BYTES * 2;
        if (clazz == ParameterId.class) return Short.BYTES;
        if (clazz == Duration.class) return Integer.BYTES * 2;
        if (clazz == LocatorKind.class) return Integer.BYTES;
        if (clazz == GuidPrefix.class) return GuidPrefix.SIZE;
        if (clazz == ProtocolVersion.class) return 2;
        if (clazz == VendorId.class) return 2;
        if (clazz == InfoTimestamp.class) return getFixedLength(Timestamp.class);
        if (clazz == InfoDestination.class) return getFixedLength(GuidPrefix.class);
        if (clazz == RepresentationIdentifier.class) return RepresentationIdentifier.SIZE;
        if (clazz == SerializedPayloadHeader.class)
            return SerializedPayloadHeader.SIZE + getFixedLength(RepresentationIdentifier.class);
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
            return Integer.BYTES + getFixedLength(Duration.class);
        if (clazz == DestinationOrderQosPolicy.class) return Integer.BYTES;
        if (clazz == DurabilityQosPolicy.class) return Integer.BYTES;
        if (clazz == DurabilityServiceQosPolicy.class)
            return getFixedLength(Duration.class)
                    + getFixedLength(HistoryQosPolicy.class)
                    + Integer.BYTES * 4;
        if (clazz == HistoryQosPolicy.class) return Integer.BYTES;
        if (clazz == KeyHash.class) return KeyHash.SIZE;
        if (clazz == Heartbeat.class)
            return getFixedLength(EntityId.class) * 2
                    + getFixedLength(SequenceNumber.class) * 2
                    + getFixedLength(Count.class);
        return -1;
    }

    public int calculateLength(Object obj) {
        var len = getFixedLengthInternal(obj.getClass());
        if (len != -1) return len;
        if (obj instanceof Data d)
            return Short.BYTES * 2
                    + getFixedLength(EntityId.class) * 2
                    + getFixedLength(SequenceNumber.class)
                    + calculateUserParameterListLength(d.inlineQos)
                    + calculateLength(d.serializedPayload);
        if (obj instanceof SerializedPayload p)
            return getFixedLength(SerializedPayloadHeader.class) + calculateLength(p.payload);
        if (obj instanceof ParameterList pl)
            return calculateParameterLength(Map.entry(ParameterId.PID_SENTINEL, 0))
                    + pl.getParameters().entrySet().stream()
                            .mapToInt(this::calculateParameterLength)
                            .sum();
        if (obj instanceof String s) return s.length() + 1 + Integer.BYTES;
        if (obj instanceof UserDataQosPolicy policy) return calculateLength(policy.value);
        if (obj instanceof ByteSequence seq) return Integer.BYTES + seq.length;
        if (obj instanceof AckNack ackNack) {
            return getFixedLength(EntityId.class) * 2
                    + calculateLength(ackNack.readerSNState)
                    + getFixedLength(Count.class);
        }
        if (obj instanceof SequenceNumberSet set)
            return getFixedLength(SequenceNumber.class)
                    + Integer.BYTES
                    + Integer.BYTES * set.bitmap.length;
        if (obj instanceof IntSequence intSeq)
            return Integer.BYTES + Integer.BYTES * intSeq.data.length;
        if (obj instanceof RawData rawData) return rawData.getData().length;
        throw new XRE("Cannot calculate length for an object of type %s", obj.getClass().getName());
    }

    private int calculateParameterLength(Entry<ParameterId, ?> param) {
        return getFixedLength(ParameterId.class)
                + Short.BYTES /* length */
                + calculateParameterValueLength(param);
    }

    public int calculateParameterValueLength(Entry<ParameterId, ?> param) {
        ParameterId id = param.getKey();
        var len =
                switch (id) {
                    case PID_ENTITY_NAME -> calculateLength(param.getValue());
                    case PID_TOPIC_NAME -> calculateLength(param.getValue());
                    case PID_TYPE_NAME -> calculateLength(param.getValue());
                    case PID_SENTINEL -> 0;
                    case PID_USER_DATA -> calculateLength(param.getValue());
                    case PID_KEY_HASH,
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
                            PID_RELIABILITY,
                            PID_DURABILITY,
                            PID_DURABILITY_SERVICE,
                            PID_DESTINATION_ORDER -> getFixedLength(id.getParameterClass());
                    default -> throw new XRE(
                            "Cannot calculate length for an unknown parameter id %s", id);
                };

        return len + calculateParameterListValuePadding(len);
    }

    private int calculateParameterListValuePadding(int len) {
        @RtpsSpecReference(
                paragraph = "9.4.2.11",
                protocolVersion = Predefined.Version_2_3,
                text =
                        "ParameterList A ParameterList contains a list of Parameters, terminated"
                                + " with a sentinel. Each Parameter within the ParameterList starts"
                                + " aligned on a 4-byte boundary with respect to the start of the"
                                + " ParameterList.")
        var padding = 0;
        if (len % 4 != 0) {
            padding = 4 - (len % 4);
        }
        return padding;
    }

    private int calculateUserParameterListLength(ParameterList parameterList) {
        var params = parameterList.getUserParameters();
        if (params.isEmpty()) return 0;
        return Short.BYTES /* param id */
                + Short.BYTES /* length */
                + calculateParameterLength(Map.entry(ParameterId.PID_SENTINEL, 0))
                + params.entrySet().stream().mapToInt(this::calculateUserParameterLength).sum();
    }

    public int calculateUserParameterLength(Entry<Short, byte[]> param) {
        var len = param.getValue().length;
        return len + calculateParameterListValuePadding(len);
    }
}
