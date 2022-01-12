package pinorobotics.rtpstalk;

import java.net.InetAddress;
import java.net.NetworkInterface;

import id.xfunction.lang.XRE;

public record RtpsTalkConfiguration(
		String networkIface,
		int userEndpointsPort,
		int builtInEnpointsPort,
		int packetBufferSize,
		InetAddress ipAddress) {

	public static final RtpsTalkConfiguration DEFAULT = new RtpsTalkConfiguration(
			"lo", 3912, 3913, 1024, getNetworkIfaceIp("lo"));

	private static InetAddress getNetworkIfaceIp(String networkIface) {
		try {
			return NetworkInterface.getByName(networkIface).getInterfaceAddresses().get(0).getAddress();
		} catch (Exception e) {
			throw new XRE("Error obtaining IP address for network interface %s", networkIface);
		}
	}
}
