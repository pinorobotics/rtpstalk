package pinorobotics.rtpstalk.io;

import id.xfunction.lang.XRE;
import java.util.Map;
import java.util.Map.Entry;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.messages.ByteSequence;
import pinorobotics.rtpstalk.messages.Duration;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.LocatorKind;
import pinorobotics.rtpstalk.messages.UserDataQosPolicy;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.messages.submessages.RepresentationIdentifier;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.messages.submessages.SubmessageHeader;
import pinorobotics.rtpstalk.messages.submessages.SubmessageKind;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.Timestamp;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;

public class LengthCalculator {

    private static LengthCalculator calculator = new LengthCalculator();
    public static final int ADDRESS_SIZE = 16;

    public static LengthCalculator getInstance() {
        return calculator;
    }

    public int getFixedLength(Class<?> clazz) {
        var len = getFixedLengthInternal(clazz);
        if (len == -1)
            throw new XRE("Fixed length is unknown for type %s", clazz);
        return len;
    }

    // TODO add all to HashMap to avoid recalculations
    public int getFixedLengthInternal(Class<?> clazz) {
        if (clazz == EntityId.class)
            return EntityId.SIZE + 1;
        if (clazz == BuiltinEndpointSet.class)
            return Integer.BYTES;
        if (clazz == SequenceNumber.class)
            return Integer.BYTES * 2;
        if (clazz == Timestamp.class)
            return Integer.BYTES * 2;
        if (clazz == ParameterId.class)
            return Short.BYTES;
        if (clazz == Duration.class)
            return Integer.BYTES * 2;
        if (clazz == LocatorKind.class)
            return Integer.BYTES;
        if (clazz == GuidPrefix.class)
            return GuidPrefix.SIZE;
        if (clazz == ProtocolVersion.class)
            return 2;
        if (clazz == VendorId.class)
            return 2;
        if (clazz == InfoTimestamp.class)
            return getFixedLength(Timestamp.class);
        if (clazz == RepresentationIdentifier.class)
            return RepresentationIdentifier.SIZE;
        if (clazz == SerializedPayloadHeader.class)
            return SerializedPayloadHeader.SIZE + getFixedLength(RepresentationIdentifier.class);
        if (clazz == SubmessageKind.class)
            return 1;
        if (clazz == SubmessageHeader.class)
            return getFixedLength(SubmessageKind.class) + 1 + Short.BYTES;
        if (clazz == Locator.class)
            return getFixedLength(LocatorKind.class) + Integer.BYTES + ADDRESS_SIZE;
        if (clazz == Guid.class)
            return getFixedLength(GuidPrefix.class) + getFixedLength(EntityId.class);
        return -1;
    }

    public int calculateLength(Object obj) {
        var len = getFixedLengthInternal(obj.getClass());
        if (len != -1)
            return len;
        if (obj instanceof Data d)
            return Short.BYTES * 2 + getFixedLength(EntityId.class) * 2
                    + getFixedLength(SequenceNumber.class)
                    + calculateLength(d.serializedPayload);
        if (obj instanceof SerializedPayload p)
            return getFixedLength(SerializedPayloadHeader.class)
                    + calculateLength(p.payload);
        if (obj instanceof ParameterList pl)
            return calculateParameterLength(Map.entry(ParameterId.PID_SENTINEL, 0))
                    + pl.getParameters().entrySet().stream()
                            .mapToInt(this::calculateParameterLength)
                            .sum();
        if (obj instanceof String s)
            return s.length() + 1 + Integer.BYTES;
        if (obj instanceof UserDataQosPolicy policy)
            return calculateLength(policy.value);
        if (obj instanceof ByteSequence seq)
            return Integer.BYTES + seq.length;
        throw new XRE("Cannot calculate length for an object of type %s", obj.getClass().getName());
    }

    public int calculateParameterLength(Entry<ParameterId, ?> param) {
        return getFixedLength(ParameterId.class)
                + Short.BYTES /* length */
                + calculateParameterValueLength(param);
    }

    public int calculateParameterValueLength(Entry<ParameterId, ?> param) {
        ParameterId id = param.getKey();
        var len = switch (id) {
        case PID_ENTITY_NAME -> calculateLength(param.getValue());
        case PID_BUILTIN_ENDPOINT_SET -> getFixedLength(BuiltinEndpointSet.class);
        case PID_PARTICIPANT_LEASE_DURATION -> getFixedLength(Duration.class);
        case PID_DEFAULT_UNICAST_LOCATOR, PID_METATRAFFIC_UNICAST_LOCATOR -> getFixedLength(Locator.class);
        case PID_PARTICIPANT_GUID -> getFixedLength(Guid.class);
        case PID_PROTOCOL_VERSION -> getFixedLength(ProtocolVersion.class);
        case PID_VENDORID -> getFixedLength(VendorId.class);
        case PID_SENTINEL -> 0;
        case PID_USER_DATA -> calculateLength(param.getValue());
        default -> throw new XRE("Cannot calculate length for an unknown parameter id %s", id);
        };

        /*
         * 9.4.2.11 ParameterList A ParameterList contains a list of Parameters,
         * terminated with a sentinel. Each Parameter within the ParameterList starts
         * aligned on a 4-byte boundary with respect to the start of the ParameterList.
         */
        var padding = 0;
        if (len % 4 != 0) {
            padding = 4 - (len % 4);
        }
        return len + padding;
    }
}
