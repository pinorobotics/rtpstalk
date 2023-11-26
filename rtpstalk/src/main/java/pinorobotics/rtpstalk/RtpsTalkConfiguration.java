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

import id.xfunction.Preconditions;
import id.xfunction.XByte;
import id.xfunction.XJsonStringBuilder;
import id.xfunction.function.Unchecked;
import java.net.NetworkInterface;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscriber;
import java.util.function.Supplier;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;

/**
 * Configuration for {@link RtpsTalkClient}.
 *
 * <p>Detailed description of parameters see in {@link Builder}
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 * @author lambdaprime intid@protonmail.com
 */
public record RtpsTalkConfiguration(
        int startPort,
        int historyCacheMaxSize,
        Optional<Integer> builtinEnpointsPort,
        Optional<Integer> userEndpointsPort,
        Optional<NetworkInterface> networkInterface,
        int packetBufferSize,
        int domainId,
        byte[] guidPrefix,
        byte[] localParticipantGuid,
        EndpointQos builtinEndpointQos,
        Duration leaseDuration,
        Duration heartbeatPeriod,
        Duration spdpDiscoveredParticipantDataPublishPeriod,
        int appEntityKey,
        Optional<ExecutorService> publisherExecutor,
        int publisherMaxBufferSize,
        int receiveBufferSize,
        int sendBufferSize) {

    /**
     * A UDP datagram is carried in a single IP packet and is hence limited to a maximum payload of
     * 65,507 bytes for IPv4
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8085">UDP RFC</a>
     */
    public static final int UDP_MAX_PACKET_SIZE = 65_508;

    public static final int MIN_PACKET_BUFFER_SIZE = 10_000;

    public RtpsTalkConfiguration {
        Preconditions.isTrue(
                packetBufferSize >= MIN_PACKET_BUFFER_SIZE,
                "packetBufferSize is less than %s",
                MIN_PACKET_BUFFER_SIZE);
        Preconditions.isTrue(
                packetBufferSize <= UDP_MAX_PACKET_SIZE,
                "packetBufferSize cannot exceed UDP packet maximum size");
        Preconditions.isTrue(
                packetBufferSize % 4 == 0, "packetBufferSize must be aligned on 32-bit boundary");
    }

    /** E=0 means big-endian, E=1 means little-endian. */
    public static final int ENDIANESS_BIT = 0b1;

    public static ByteOrder getByteOrder() {
        return switch (ENDIANESS_BIT) {
            case 0b1 -> ByteOrder.LITTLE_ENDIAN;
            default -> ByteOrder.LITTLE_ENDIAN;
        };
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("startPort", startPort);
        builder.append("networkIfaces", networkInterface);
        builder.append("packetBufferSize", packetBufferSize);
        builder.append("domainId", domainId);
        builder.append("guidPrefix", XByte.toHex(guidPrefix));
        builder.append("builtinEndpointQos", builtinEndpointQos);
        builder.append("leaseDuration", leaseDuration);
        builder.append("builtinEnpointsPort", builtinEnpointsPort);
        builder.append("userEndpointsPort", userEndpointsPort);
        builder.append("historyCacheMaxSize", historyCacheMaxSize);
        builder.append("publisherExecutor", publisherExecutor);
        builder.append("receiveBufferSize", receiveBufferSize);
        return builder.toString();
    }

    public static class Builder {

        /**
         * Default starting port from which port assignment for {@link #builtinEnpointsPort}, {@link
         * #userEndpointsPort} will happen.
         *
         * <p><b>rtpstalk</b> will start to look for available ports starting from this port. If the
         * port is in use it is skipped and next is tested.
         */
        public static final int DEFAULT_START_PORT = 7412;

        /**
         * Maximum size of RTPS HistoryCache.
         *
         * <p>When Writer HistoryCache becomes full it blocks user Publisher from submitting new
         * messages.
         */
        public static final int DEFAULT_HISTORY_CACHE_MAX_SIZE = 100;

        public static final Supplier<ExecutorService> DEFAULT_PUBLISHER_EXECUTOR =
                () -> Executors.newCachedThreadPool();
        public static final int DEFAULT_PUBLISHER_BUFFER_SIZE = 32;

        public static final int DEFAULT_RECEIVE_BUFFER_SIZE = 26_214_400;

        public static final int DEFAULT_SEND_BUFFER_SIZE = 26_214_400;

        public static final int DEFAULT_DOMAIN_ID = 0;

        private Optional<NetworkInterface> networkIface = Optional.empty();
        private int startPort = DEFAULT_START_PORT;
        private Optional<Integer> builtinEnpointsPort = Optional.empty();
        private Optional<Integer> userEndpointsPort = Optional.empty();
        private int packetBufferSize = UDP_MAX_PACKET_SIZE;
        private int domainId = DEFAULT_DOMAIN_ID;
        private int appEntityKey = 0x000012;
        private byte[] guidPrefix = GuidPrefix.generate().value;
        private EndpointQos builtinEndpointQos = EndpointQos.NONE;
        private Duration leaseDuration = Duration.ofSeconds(20);
        private Duration heartbeatPeriod = Duration.ofSeconds(1);
        private Duration spdpDiscoveredParticipantDataPublishPeriod = Duration.ofSeconds(5);
        private Optional<ExecutorService> publisherExecutor = Optional.empty();
        private int publisherMaxBufferCapacity = DEFAULT_PUBLISHER_BUFFER_SIZE;
        private int historyCacheMaxSize = DEFAULT_HISTORY_CACHE_MAX_SIZE;
        private int receiveBufferSize = DEFAULT_RECEIVE_BUFFER_SIZE;
        private int sendBufferSize = DEFAULT_SEND_BUFFER_SIZE;

        /**
         * @see #networkInterface(String)
         */
        public Builder networkInterface(NetworkInterface networkIface) {
            this.networkIface = Optional.of(networkIface);
            return this;
        }

        /**
         * Network interface where <b>rtpstalk</b> will be running.
         *
         * <p>By default it runs SPDP on all network interfaces but announces itself only on one of
         * them.
         *
         * <p>Users can limit <b>rtpstalk</b> traffic to local network by specifying only its
         * loopback interface.
         *
         * <p><b>rtpstalk</b> currently supports only network interfaces with IPv4 addresses.
         */
        public Builder networkInterface(String networkIface) {
            this.networkIface =
                    Optional.of(Unchecked.get(() -> NetworkInterface.getByName(networkIface)));
            return this;
        }

        /**
         * {@link ExecutorService} which will be used by all <b>rtpstalk</b> internal publishers.
         *
         * <p>By default each instance of the client will use {@link ExecutorService} provided by
         * {@link DEFAULT_PUBLISHER_EXECUTOR} This method allows to set custom {@link
         * ExecutorService}. In that case {@link ExecutorService} should be managed by the user and
         * terminated properly.
         */
        public Builder publisherExecutor(ExecutorService publisherExecutor) {
            this.publisherExecutor = Optional.of(publisherExecutor);
            return this;
        }

        public Builder publisherMaxBufferSize(int publisherMaxBufferCapacity) {
            this.publisherMaxBufferCapacity = publisherMaxBufferCapacity;
            return this;
        }

        /** Port on which all RTPS built-in endpoints will be running. */
        public Builder builtinEnpointsPort(int builtInEnpointsPort) {
            this.builtinEnpointsPort = Optional.of(builtInEnpointsPort);
            return this;
        }

        /** Port on which all User Data endpoints will be running. */
        public Builder userEndpointsPort(int userEndpointsPort) {
            this.userEndpointsPort = Optional.of(userEndpointsPort);
            return this;
        }

        /** Maximum size of RTPS packets */
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

        public Builder historyCacheMaxSize(int historyCacheMaxSize) {
            this.historyCacheMaxSize = historyCacheMaxSize;
            return this;
        }

        /** GuidPrefix of the local Participant (by default it is randomly generated) */
        public Builder guidPrefix(byte[] guidPrefix) {
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

        /** Heartbeat period for Statefull Reliable DataWriter */
        public Builder heartbeatPeriod(Duration heartbeatPeriod) {
            this.heartbeatPeriod = heartbeatPeriod;
            return this;
        }

        /** Announcement period for SPDP */
        public Builder spdpDiscoveredParticipantDataPublishPeriod(
                Duration spdpDiscoveredParticipantDataPublishPeriod) {
            this.spdpDiscoveredParticipantDataPublishPeriod =
                    spdpDiscoveredParticipantDataPublishPeriod;
            return this;
        }

        /**
         * When Writer sends many large messages the network socket receive buffer on the Reader may
         * quickly run out of space. When receive buffer is full all new messages are dropped.
         * Depending on QOS policy when some of the messages are lost then {@link Subscriber} may
         * stop receiving new messages.
         *
         * <p>Increasing receive buffer allows to store more messages and to reduce number of lost
         * messages.
         *
         * <p>Another way to address message loss is to configure QOS policy which is not available
         * at that moment.
         */
        public Builder receiveBufferSize(int receiveBufferSize) {
            this.receiveBufferSize = receiveBufferSize;
            return this;
        }

        /**
         * When Writer sends many large messages it adds them to the system UDP network queue. When
         * Writer publishes them faster than system sends them this queue will run out of space and
         * result in:
         *
         * <pre>{@code
         * java.io.IOException: No buffer space available
         * at java.base/sun.nio.ch.DatagramDispatcher.write0(Native Method)
         * at java.base/sun.nio.ch.DatagramDispatcher.write(DatagramDispatcher.java:51)
         * at java.base/sun.nio.ch.IOUtil.writeFromNativeBuffer(IOUtil.java:132)
         * at java.base/sun.nio.ch.IOUtil.write(IOUtil.java:97)
         * at java.base/sun.nio.ch.IOUtil.write(IOUtil.java:53)
         * at java.base/sun.nio.ch.DatagramChannelImpl.send(DatagramChannelImpl.java:797)
         * at rtpstalk/pinorobotics.rtpstalk.impl.spec.transport.DataChannel.send(DataChannel.java:123)
         * }</pre>
         *
         * <p>Increasing send buffer allows to store more messages giving more time system to send
         * them.
         */
        public Builder sendBufferSize(int sendBufferSize) {
            this.sendBufferSize = sendBufferSize;
            return this;
        }

        public RtpsTalkConfiguration build() {
            return new RtpsTalkConfiguration(
                    startPort,
                    historyCacheMaxSize,
                    builtinEnpointsPort,
                    userEndpointsPort,
                    networkIface,
                    packetBufferSize,
                    domainId,
                    guidPrefix,
                    new Guid(guidPrefix, EntityId.Predefined.ENTITYID_PARTICIPANT).toArray(),
                    builtinEndpointQos,
                    leaseDuration,
                    heartbeatPeriod,
                    spdpDiscoveredParticipantDataPublishPeriod,
                    appEntityKey,
                    publisherExecutor,
                    publisherMaxBufferCapacity,
                    receiveBufferSize,
                    sendBufferSize);
        }
    }
}
