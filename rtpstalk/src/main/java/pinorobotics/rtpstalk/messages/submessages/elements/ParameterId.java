package pinorobotics.rtpstalk.messages.submessages.elements;

import java.util.HashMap;
import java.util.Map;

public enum ParameterId {

    PID_ENTITY_NAME(0x0062),
    PID_BUILTIN_ENDPOINT_SET(0x0058),
    PID_PARTICIPANT_LEASE_DURATION(0x0002),
    PID_DEFAULT_UNICAST_LOCATOR(0x0031),
    PID_UNICAST_LOCATOR(0x002f),
    PID_METATRAFFIC_UNICAST_LOCATOR(0x0032),
    PID_PARTICIPANT_GUID(0x0050),
    PID_VENDORID(0x0016),
    PID_PROTOCOL_VERSION(0x0015),
    PID_USER_DATA(0x002c),
    PID_TOPIC_NAME(0x0005),
    PID_EXPECTS_INLINE_QOS(0x0043),
    PID_TYPE_NAME(0x0007),
    PID_ENDPOINT_GUID(0x005a),
    PID_BUILTIN_ENDPOINT_QOS(0x0077),
    PID_KEY_HASH(0x0070),
    PID_SENTINEL(0x0001);

    public static Map<Short, ParameterId> map = new HashMap<>();
    static {
        for (var t : ParameterId.values())
            map.put(t.getValue(), t);
    }

    private short value;

    ParameterId(int i) {
        this.value = (short) i;
    }

    public short getValue() {
        return value;
    }
}
