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

import static java.util.stream.Collectors.toList;

import id.xfunction.XJsonStringBuilder;
import id.xfunction.function.LazyInitializer;
import id.xfunction.function.Unchecked;
import id.xfunction.net.FreePortIterator;
import id.xfunction.net.FreePortIterator.Protocol;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import pinorobotics.rtpstalk.messages.BuiltinEndpointQos.EndpointQos;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 * @author lambdaprime intid@protonmail.com
 */
public record RtpsTalkConfiguration(
        List<RtpsNetworkInterface> networkInterfaces,
        int packetBufferSize,
        int domainId,
        GuidPrefix guidPrefix,
        Guid localParticpantGuid,
        EndpointQos builtinEndpointQos,
        Duration leaseDuration,
        Duration heartbeatPeriod,
        Duration spdpDiscoveredParticipantDataPublishPeriod,
        int appEntityKey,
        Executor publisherExecutor,
        int publisherMaxBufferCapacity) {

    /** E=0 means big-endian, E=1 means little-endian. */
    public static final int ENDIANESS_BIT = 0b1;

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("networkIfaces", networkInterfaces);
        builder.append("packetBufferSize", packetBufferSize);
        builder.append("domainId", domainId);
        builder.append("guidPrefix", guidPrefix);
        builder.append("builtinEndpointQos", builtinEndpointQos);
        builder.append("leaseDuration", leaseDuration);
        builder.append("localParticpantGuid", localParticpantGuid);
        return builder.toString();
    }

    public static class Builder {

        /**
         * A UDP datagram is carried in a single IP packet and is hence limited to a maximum payload
         * of 65,507 bytes for IPv4"
         *
         * <p>https://datatracker.ietf.org/doc/html/rfc8085
         */
        public static final int UDP_MAX_PACKET_SIZE = 65_507;

        /**
         * Default starting port from which port assignment for {@link #builtInEnpointsPort}, {@link
         * #userEndpointsPort} will happen
         */
        public static final int DEFAULT_START_PORT = 7412;

        public static final Executor DEFAULT_PUBLISHER_EXECUTOR = ForkJoinPool.commonPool();
        public static final int DEFAULT_PUBLISHER_BUFFER_SIZE = Flow.defaultBufferSize();

        private List<NetworkInterface> networkIfaces = listAllNetworkInterfaces();
        private int startPort = DEFAULT_START_PORT;
        private int builtInEnpointsPort;
        private int userEndpointsPort;
        private int packetBufferSize = UDP_MAX_PACKET_SIZE;
        private int domainId = 0;
        private int appEntityKey = 0x000012;
        private GuidPrefix guidPrefix = GuidPrefix.generate();
        private EndpointQos builtinEndpointQos = EndpointQos.NONE;
        private Duration leaseDuration = Duration.ofSeconds(20);
        private Duration heartbeatPeriod = Duration.ofSeconds(1);
        private Duration spdpDiscoveredParticipantDataPublishPeriod = Duration.ofSeconds(5);
        private Executor publisherExecutor = DEFAULT_PUBLISHER_EXECUTOR;
        private int publisherMaxBufferCapacity = DEFAULT_PUBLISHER_BUFFER_SIZE;

        public Builder networkInterfaces(List<NetworkInterface> networkIfaces) {
            this.networkIfaces = networkIfaces;
            return this;
        }

        /**
         * List of network interfaces where <b>rtpstalk</b> will be working on. By default it is
         * active on all network interfaces and it publishes and receives data from all of them.
         * Users can limit <b>rtpstalk</b> traffic to local network by specifying only its loopback
         * interface.
         */
        public Builder networkInterfaces(String... networkIfaces) {
            this.networkIfaces =
                    Arrays.stream(networkIfaces)
                            .map(Unchecked.wrapApply(NetworkInterface::getByName))
                            .collect(toList());
            return this;
        }

        public Builder builtinEnpointsPort(int builtInEnpointsPort) {
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
            var portIterator = new FreePortIterator(startPort, Protocol.UDP);
            var builtInEnpointsPortSupplier =
                    new LazyInitializer<Integer>(
                            () ->
                                    builtInEnpointsPort != 0
                                            ? builtInEnpointsPort
                                            : portIterator.next());
            var userEndpointsPortSupplier =
                    new LazyInitializer<Integer>(
                            () -> userEndpointsPort != 0 ? userEndpointsPort : portIterator.next());
            var rtpsNetworkIfaces =
                    networkIfaces.stream()
                            .map(
                                    iface ->
                                            new RtpsNetworkInterface(
                                                    domainId,
                                                    iface,
                                                    builtInEnpointsPortSupplier,
                                                    userEndpointsPortSupplier))
                            .collect(toList());

            return new RtpsTalkConfiguration(
                    rtpsNetworkIfaces,
                    packetBufferSize,
                    domainId,
                    guidPrefix,
                    new Guid(guidPrefix, EntityId.Predefined.ENTITYID_PARTICIPANT.getValue()),
                    builtinEndpointQos,
                    leaseDuration,
                    heartbeatPeriod,
                    spdpDiscoveredParticipantDataPublishPeriod,
                    appEntityKey,
                    publisherExecutor,
                    publisherMaxBufferCapacity);
        }

        private static List<NetworkInterface> listAllNetworkInterfaces() {
            try {
                return NetworkInterface.networkInterfaces().collect(toList());
            } catch (SocketException e) {
                throw new RuntimeException("Error listing available network interfaces", e);
            }
        }
    }

    public Guid getLocalParticipantGuid() {
        return localParticpantGuid;
    }
}
