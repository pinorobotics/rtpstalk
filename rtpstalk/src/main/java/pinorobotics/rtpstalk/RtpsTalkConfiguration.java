package pinorobotics.rtpstalk;

import java.net.InetAddress;
import java.net.NetworkInterface;

import id.xfunction.lang.XRE;

public record RtpsTalkConfiguration(
		String networkIface,
		int builtInEnpointsPort,
		int userEndpointsPort,
		int packetBufferSize,
		InetAddress ipAddress) {

	private static final String DEFAULT_NETWORK_IFACE = "eth0";
	
	public static final RtpsTalkConfiguration DEFAULT = new RtpsTalkConfiguration(
			DEFAULT_NETWORK_IFACE, 7412, 7413, 1024, getNetworkIfaceIp(DEFAULT_NETWORK_IFACE));

	private static InetAddress getNetworkIfaceIp(String networkIface) {
		try {
			return NetworkInterface.getByName(networkIface).getInterfaceAddresses().get(0).getAddress();
		} catch (Exception e) {
			throw new XRE("Error obtaining IP address for network interface %s", networkIface);
		}
	}
}
