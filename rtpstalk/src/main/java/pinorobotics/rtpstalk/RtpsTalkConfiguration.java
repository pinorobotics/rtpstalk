package pinorobotics.rtpstalk;

import java.net.InetAddress;
import java.net.NetworkInterface;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import id.xfunction.lang.XRE;

public record RtpsTalkConfiguration(
        String networkIface,
        int builtInEnpointsPort,
        int userEndpointsPort,
        int packetBufferSize,
        int domainId,
        InetAddress ipAddress,
        GuidPrefix guidPrefix) {

    private static final String DEFAULT_NETWORK_IFACE = "eth0";

    /**
     * E=0 means big-endian, E=1 means little-endian.
     */
    public static final int ENDIANESS_BIT = 0b1;

    public static final RtpsTalkConfiguration DEFAULT = new RtpsTalkConfiguration(
            DEFAULT_NETWORK_IFACE, 7412, 7413, 1024, 0, getNetworkIfaceIp(DEFAULT_NETWORK_IFACE),
            GuidPrefix.generate());

    private static InetAddress getNetworkIfaceIp(String networkIface) {
        try {
            return NetworkInterface.getByName(networkIface).getInterfaceAddresses().get(0).getAddress();
        } catch (Exception e) {
            throw new XRE("Error obtaining IP address for network interface %s", networkIface);
        }
    }
}
