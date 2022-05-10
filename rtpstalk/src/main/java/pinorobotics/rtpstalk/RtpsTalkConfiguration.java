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
import id.xfunction.function.Unchecked;
import java.net.NetworkInterface;
import java.time.Duration;
import java.util.Optional;
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
        int startPort,
        Optional<Integer> builtInEnpointsPort,
        Optional<Integer> userEndpointsPort,
        Optional<NetworkInterface> networkInterface,
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
        int publisherMaxBufferSize) {

    /** E=0 means big-endian, E=1 means little-endian. */
    public static final int ENDIANESS_BIT = 0b1;

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("startPort", startPort);
        builder.append("networkIfaces", networkInterface);
        builder.append("packetBufferSize", packetBufferSize);
        builder.append("domainId", domainId);
        builder.append("guidPrefix", guidPrefix);
        builder.append("builtinEndpointQos", builtinEndpointQos);
        builder.append("leaseDuration", leaseDuration);
        builder.append("localParticpantGuid", localParticpantGuid);
        builder.append("builtInEnpointsPort", builtInEnpointsPort);
        builder.append("userEndpointsPort", userEndpointsPort);
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

        private Optional<NetworkInterface> networkIface = Optional.empty();
        private int startPort = DEFAULT_START_PORT;
        private Optional<Integer> builtInEnpointsPort = Optional.empty();
        private Optional<Integer> userEndpointsPort = Optional.empty();
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

        public Builder networkInterfaces(NetworkInterface networkIface) {
            this.networkIface = Optional.of(networkIface);
            return this;
        }

        /**
         * Network interface where <b>rtpstalk</b> will be working on. By default it is active on
         * all network interfaces and it publishes and receives data from all of them. Users can
         * limit <b>rtpstalk</b> traffic to local network by specifying only its loopback interface.
         */
        public Builder networkInterface(String networkIface) {
            this.networkIface =
                    Optional.of(Unchecked.get(() -> NetworkInterface.getByName(networkIface)));
            return this;
        }

        public Builder publisherExecutor(Executor publisherExecutor) {
            this.publisherExecutor = publisherExecutor;
            return this;
        }

        public Builder publisherMaxBufferSize(int publisherMaxBufferCapacity) {
            this.publisherMaxBufferCapacity = publisherMaxBufferCapacity;
            return this;
        }

        public Builder builtinEnpointsPort(int builtInEnpointsPort) {
            this.builtInEnpointsPort = Optional.of(builtInEnpointsPort);
            return this;
        }

        public Builder userEndpointsPort(int userEndpointsPort) {
            this.userEndpointsPort = Optional.of(userEndpointsPort);
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
            return new RtpsTalkConfiguration(
                    startPort,
                    builtInEnpointsPort,
                    userEndpointsPort,
                    networkIface,
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
    }

    public Guid getLocalParticipantGuid() {
        return localParticpantGuid;
    }
}
