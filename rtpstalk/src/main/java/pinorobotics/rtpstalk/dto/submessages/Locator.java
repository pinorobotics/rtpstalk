package pinorobotics.rtpstalk.dto.submessages;

import java.net.InetAddress;
import java.net.UnknownHostException;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.spdp.PortNumberParameters;

public record Locator(LocatorKind kind, int port, InetAddress address) {

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
}
