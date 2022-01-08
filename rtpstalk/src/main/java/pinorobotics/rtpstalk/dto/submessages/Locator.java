package pinorobotics.rtpstalk.dto.submessages;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.spdp.PortNumberParameters;

public record Locator(LocatorKind kind, int port, String address) {

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("transportType", kind);
		builder.append("port", port);
		builder.append("address", address);
		return builder.toString();
	}

	public static Locator createDefaultMulticastLocator(int domainId) {
		return new Locator(LocatorKind.LOCATOR_KIND_UDPv4, PortNumberParameters.DEFAULT.getMultiCastPort(domainId), "239.255.0.1");
	}
}
