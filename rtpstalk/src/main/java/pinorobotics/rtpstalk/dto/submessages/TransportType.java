package pinorobotics.rtpstalk.dto.submessages;

/**
 * identifying the kind of transport
 */
public enum TransportType {

	LOCATOR_KIND_INVALID(-1),
	LOCATOR_KIND_RESERVED(0),
	LOCATOR_KIND_UDPv4(1),
	LOCATOR_KIND_UDPv6(2);
	
	private int val;

	private TransportType(int val) {
		this.val = val;
	}
	
	public int getValue() {
		return val;
	}
}
