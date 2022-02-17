package pinorobotics.rtpstalk;

import id.xfunction.XJsonStringBuilder;
import id.xfunction.lang.XRE;
import java.net.InetAddress;
import java.net.NetworkInterface;
import pinorobotics.rtpstalk.messages.BuiltinEndpointQos.EndpointQos;
import pinorobotics.rtpstalk.messages.Duration;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.LocatorKind;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;

public class RtpsTalkConfiguration {

    private static final String DEFAULT_NETWORK_IFACE = "eth0";

    /**
     * A UDP datagram is carried in a single IP packet and is hence limited to a
     * maximum payload of 65,507 bytes for IPv4"
     * 
     * https://datatracker.ietf.org/doc/html/rfc8085
     */
    private static final int UDP_MAX_PACKET_SIZE = 65_507;

    /**
     * E=0 means big-endian, E=1 means little-endian.
     */
    public static final int ENDIANESS_BIT = 0b1;

    public static final RtpsTalkConfiguration DEFAULT = new RtpsTalkConfiguration(
            DEFAULT_NETWORK_IFACE, 7412, 7413, UDP_MAX_PACKET_SIZE, 0, getNetworkIfaceIp(DEFAULT_NETWORK_IFACE),
            GuidPrefix.generate(), EndpointQos.NONE, new Duration(20), new Duration(1));

    private String networkIface;
    private int builtInEnpointsPort;
    private int userEndpointsPort;
    private int packetBufferSize;
    private int domainId;
    private InetAddress ipAddress;
    private GuidPrefix guidPrefix;
    private EndpointQos builtinEndpointQos;
    private Locator defaultUnicastLocator;
    private Locator metatrafficUnicastLocator;
    private Duration leaseDuration;
    private Locator metatrafficMulticastLocator;
    private Duration heartbeatPeriod;

    public RtpsTalkConfiguration(String networkIface,
            int builtInEnpointsPort,
            int userEndpointsPort,
            int packetBufferSize,
            int domainId,
            InetAddress ipAddress,
            GuidPrefix guidPrefix,
            EndpointQos builtinEndpointQos,
            Duration leaseDuration,
            Duration heartbeatPeriod) {
        this.networkIface = networkIface;
        this.builtInEnpointsPort = builtInEnpointsPort;
        this.userEndpointsPort = userEndpointsPort;
        this.packetBufferSize = packetBufferSize;
        this.domainId = domainId;
        this.ipAddress = ipAddress;
        this.guidPrefix = guidPrefix;
        this.builtinEndpointQos = builtinEndpointQos;
        this.leaseDuration = leaseDuration;
        this.heartbeatPeriod = heartbeatPeriod;
        defaultUnicastLocator = new Locator(
                LocatorKind.LOCATOR_KIND_UDPv4, userEndpointsPort, ipAddress);
        metatrafficUnicastLocator = new Locator(LocatorKind.LOCATOR_KIND_UDPv4, builtInEnpointsPort, ipAddress);
        metatrafficMulticastLocator = Locator.createDefaultMulticastLocator(domainId);
    }

    /**
     * List of unicast locators (transport, address, port combinations) that can be
     * used to send messages to the built-in Endpoints contained in the Participant.
     * 
     * Currently only one locator supported.
     */
    public Locator getMetatrafficUnicastLocator() {
        return metatrafficUnicastLocator;
    }

    /**
     * Default list of unicast locators (transport, address, port combinations) that
     * can be used to send messages to the user-defined Endpoints contained in the
     * Participant. These are the unicast locators that will be used in case the
     * Endpoint does not specify its own set of Locators, so at least one Locator
     * must be present.
     * 
     * Currently only one locator supported.
     */
    public Locator getDefaultUnicastLocator() {
        return defaultUnicastLocator;
    }

    public String getNetworkIface() {
        return networkIface;
    }

    public int getBuiltInEnpointsPort() {
        return builtInEnpointsPort;
    }

    public int getUserEndpointsPort() {
        return userEndpointsPort;
    }

    public int getPacketBufferSize() {
        return packetBufferSize;
    }

    public int getDomainId() {
        return domainId;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public GuidPrefix getGuidPrefix() {
        return guidPrefix;
    }

    public EndpointQos getBuiltinEndpointQos() {
        return builtinEndpointQos;
    }

    public Duration getLeaseDuration() {
        return leaseDuration;
    }

    private static InetAddress getNetworkIfaceIp(String networkIface) {
        try {
            return NetworkInterface.getByName(networkIface).getInterfaceAddresses().get(0).getAddress();
        } catch (Exception e) {
            throw new XRE("Error obtaining IP address for network interface %s", networkIface);
        }
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("networkIface", networkIface);
        builder.append("builtInEnpointsPort", builtInEnpointsPort);
        builder.append("userEndpointsPort", userEndpointsPort);
        builder.append("packetBufferSize", packetBufferSize);
        builder.append("domainId", domainId);
        builder.append("ipAddress", ipAddress);
        builder.append("guidPrefix", guidPrefix);
        builder.append("builtinEndpointQos", builtinEndpointQos);
        builder.append("defaultUnicastLocator", defaultUnicastLocator);
        builder.append("metatrafficUnicastLocator", metatrafficUnicastLocator);
        builder.append("leaseDuration", leaseDuration);
        return builder.toString();
    }

    public Locator getMetatrafficMulticastLocator() {
        return metatrafficMulticastLocator;
    }

    public Duration getHeartbeatPeriod() {
        return heartbeatPeriod;
    }

}
