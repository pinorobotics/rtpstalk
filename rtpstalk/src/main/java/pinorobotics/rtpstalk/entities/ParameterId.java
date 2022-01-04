package pinorobotics.rtpstalk.entities;

import java.util.HashMap;
import java.util.Map;

public enum ParameterId {

	PID_ENTITY_NAME(0x0062),
	PID_BUILTIN_ENDPOINT_SET(0x0058),
	PID_PARTICIPANT_LEASE_DURATION(0x0002),
	PID_DEFAULT_UNICAST_LOCATOR(0x0031),
	PID_METATRAFFIC_UNICAST_LOCATOR(0x0032),
	PID_PARTICIPANT_GUID(0x0050),
	PID_VENDORID(0x0016),
	PID_PROTOCOL_VERSION(0x0015),
	PID_USER_DATA(0x002c),
	PID_TOPIC_NAME(0x0005);

	public static Map<Short, ParameterId> map = new HashMap<>();
	static {
		for (var t: ParameterId.values()) map.put(t.getValue(), t);
	}

	private short value;

	ParameterId(int i) {
		this.value = (short) i;
	}
	
	public short getValue() {
		return value;
	}
}
