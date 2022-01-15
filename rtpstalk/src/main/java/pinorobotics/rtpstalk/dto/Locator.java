package pinorobotics.rtpstalk.dto;

import java.net.InetAddress;
import java.net.UnknownHostException;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.spdp.PortNumberParameters;

public record Locator(LocatorKind kind, int port, InetAddress address) {

	public static final Locator EMPTY_IPV6 = createEmpty(LocatorKind.LOCATOR_KIND_UDPv6);
	public static final Locator INVALID = createEmpty(LocatorKind.LOCATOR_KIND_INVALID);
	
	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("transportType", kind);
		builder.append("port", port);
		builder.append("address", address);
		return builder.toString();
	}

	public static Locator createDefaultMulticastLocator(int domainId) {
		try {
			return new Locator(LocatorKind.LOCATOR_KIND_UDPv4, PortNumberParameters.DEFAULT.getMultiCastPort(domainId),
					InetAddress.getByName("239.255.0.1"));
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	private static Locator createEmpty(LocatorKind kind) {
		try {
			return new Locator(kind, 0, InetAddress.getByAddress(new byte[4]));
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
}
