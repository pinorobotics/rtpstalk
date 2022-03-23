/*
 * Copyright 2022 rtpstalk project
 * 
 * Website: https://github.com/pinorobotics/rtpstalk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pinorobotics.rtpstalk;

import id.xfunction.XJsonStringBuilder;
import id.xfunction.lang.XRE;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import pinorobotics.rtpstalk.messages.BuiltinEndpointQos.EndpointQos;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.LocatorKind;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;

/**
 * @author aeon_flux aeon_flux@eclipso.ch 
 * @author lambdaprime intid@protonmail.com
 */
public record RtpsTalkConfiguration(
        String networkIface,
        int builtInEnpointsPort,
        int userEndpointsPort,
        int packetBufferSize,
        int domainId,
        InetAddress ipAddress,
        GuidPrefix guidPrefix,
        EndpointQos builtinEndpointQos,

        /**
         * Default list of unicast locators (transport, address, port combinations) that can be used
         * to send messages to the user-defined Endpoints contained in the Participant. These are
         * the unicast locators that will be used in case the Endpoint does not specify its own set
         * of Locators, so at least one Locator must be present.
         *
         * <p>Currently only one locator supported.
         */
        Locator defaultUnicastLocator,

        /**
         * List of unicast locators (transport, address, port combinations) that can be used to send
         * messages to the built-in Endpoints contained in the Participant.
         *
         * <p>Currently only one locator supported.
         */
        Locator metatrafficUnicastLocator,
        Locator metatrafficMulticastLocator,
        Duration leaseDuration,
        Duration heartbeatPeriod,
        Duration spdpDiscoveredParticipantDataPublishPeriod,
        int appEntityKey) {

    /** E=0 means big-endian, E=1 means little-endian. */
    public static final int ENDIANESS_BIT = 0b1;

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

    public static class Builder {

        /**
         * A UDP datagram is carried in a single IP packet and is hence limited to a maximum payload
         * of 65,507 bytes for IPv4"
         *
         * <p>https://datatracker.ietf.org/doc/html/rfc8085
         */
        private static final int UDP_MAX_PACKET_SIZE = 65_507;

        private static final String DEFAULT_NETWORK_IFACE = "eth0";

        private String networkIface = DEFAULT_NETWORK_IFACE;
        private int builtInEnpointsPort = 7412;
        private int userEndpointsPort = 7413;
        private int packetBufferSize = UDP_MAX_PACKET_SIZE;
        private int domainId = 0;
        private int appEntityKey = 0x000012;
        private InetAddress ipAddress = getNetworkIfaceIp(DEFAULT_NETWORK_IFACE);
        private GuidPrefix guidPrefix = GuidPrefix.generate();
        private EndpointQos builtinEndpointQos = EndpointQos.NONE;
        private Duration leaseDuration = Duration.ofSeconds(20);
        private Duration heartbeatPeriod = Duration.ofSeconds(1);
        private Duration spdpDiscoveredParticipantDataPublishPeriod = Duration.ofSeconds(5);

        public Builder networkIface(String networkIface) {
            this.networkIface = networkIface;
            return this;
        }

        public Builder builtInEnpointsPort(int builtInEnpointsPort) {
            this.builtInEnpointsPort = builtInEnpointsPort;
            return this;
        }

        public Builder userEndpointsPort(int userEndpointsPort) {
            this.userEndpointsPort = userEndpointsPort;
            return this;
        }

        public Builder packetBufferSize(int packetBufferSize) {
            this.packetBufferSize = packetBufferSize;
            return this;
        }

        public Builder domainId(int domainId) {
            this.domainId = domainId;
            return this;
        }

        public Builder appEntityKey(int appEntityKey) {
            this.appEntityKey = appEntityKey;
            return this;
        }

        public Builder ipAddress(InetAddress ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder guidPrefix(GuidPrefix guidPrefix) {
            this.guidPrefix = guidPrefix;
            return this;
        }

        public Builder builtinEndpointQos(EndpointQos builtinEndpointQos) {
            this.builtinEndpointQos = builtinEndpointQos;
            return this;
        }

        public Builder leaseDuration(Duration leaseDuration) {
            this.leaseDuration = leaseDuration;
            return this;
        }

        public Builder heartbeatPeriod(Duration heartbeatPeriod) {
            this.heartbeatPeriod = heartbeatPeriod;
            return this;
        }

        public Builder spdpDiscoveredParticipantDataPublishPeriod(
                Duration spdpDiscoveredParticipantDataPublishPeriod) {
            this.spdpDiscoveredParticipantDataPublishPeriod =
                    spdpDiscoveredParticipantDataPublishPeriod;
            return this;
        }

        public RtpsTalkConfiguration build() {
            var defaultUnicastLocator =
                    new Locator(LocatorKind.LOCATOR_KIND_UDPv4, userEndpointsPort, ipAddress);
            var metatrafficUnicastLocator =
                    new Locator(LocatorKind.LOCATOR_KIND_UDPv4, builtInEnpointsPort, ipAddress);
            var metatrafficMulticastLocator = Locator.createDefaultMulticastLocator(domainId);

            return new RtpsTalkConfiguration(
                    networkIface,
                    builtInEnpointsPort,
                    userEndpointsPort,
                    packetBufferSize,
                    domainId,
                    ipAddress,
                    guidPrefix,
                    builtinEndpointQos,
                    defaultUnicastLocator,
                    metatrafficUnicastLocator,
                    metatrafficMulticastLocator,
                    leaseDuration,
                    heartbeatPeriod,
                    spdpDiscoveredParticipantDataPublishPeriod,
                    appEntityKey);
        }

        private static InetAddress getNetworkIfaceIp(String networkIface) {
            try {
                return NetworkInterface.getByName(networkIface)
                        .getInterfaceAddresses()
                        .get(0)
                        .getAddress();
            } catch (Exception e) {
                throw new XRE("Error obtaining IP address for network interface %s", networkIface);
            }
        }
    }
}
