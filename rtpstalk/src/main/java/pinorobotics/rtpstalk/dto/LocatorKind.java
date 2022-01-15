package pinorobotics.rtpstalk.dto;

import java.util.HashMap;
import java.util.Map;

public enum LocatorKind {

	LOCATOR_KIND_INVALID(-1),
	LOCATOR_KIND_RESERVED(0),
	LOCATOR_KIND_UDPv4(1),
	LOCATOR_KIND_UDPv6(2);

	public int value;

	public static Map<Integer, LocatorKind> VALUES = new HashMap<>();
	static {
		for (var t: values()) VALUES.put(t.value, t);
	}
	
	LocatorKind(int value) {
		this.value = value;
	}
}
