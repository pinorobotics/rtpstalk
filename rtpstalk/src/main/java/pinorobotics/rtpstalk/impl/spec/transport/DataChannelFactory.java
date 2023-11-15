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
package pinorobotics.rtpstalk.impl.spec.transport;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import id.xfunction.net.FreeUdpPortIterator;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.util.Optional;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataChannelFactory {

    private static final XLogger LOGGER = XLogger.getLogger(DataChannelFactory.class);
    private RtpsTalkConfiguration config;

    public DataChannelFactory(RtpsTalkConfiguration config) {
        this.config = config;
    }

    /** Channel bind to local port */
    public DataChannel bindMulticast(
            TracingToken tracingToken, NetworkInterface networkInterface, Locator locator)
            throws IOException {
        var socketAddress = locator.getSocketAddress();
        Preconditions.isTrue(
                socketAddress.getAddress().isMulticastAddress(), "Multicast address required");
        // see
        // https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/channels/MulticastChannel.html
        var dataChannel =
                DatagramChannel.open(StandardProtocolFamily.INET)
                        .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                        .bind(new InetSocketAddress(locator.getSocketAddress().getPort()))
                        .setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
        dataChannel.join(locator.address(), networkInterface);
        return new DataChannel(
                tracingToken,
                dataChannel,
                socketAddress,
                config.guidPrefix(),
                config.packetBufferSize());
    }

    /**
     * Bind channel to local port
     *
     * @param address if empty then bind to all network interfaces
     * @param port if empty then pick up first available
     */
    public DataChannel bind(
            TracingToken tracingToken, Optional<InetAddress> address, Optional<Integer> port)
            throws IOException {
        DatagramChannel dataChannel = null;
        if (port.isEmpty()) {
            var portIterator = new FreeUdpPortIterator(config.startPort());
            address.ifPresent(portIterator::withNetworkInterfaceAddress);
            portIterator.withSocketConfigurator(this::configure);
            dataChannel = portIterator.next();
        } else {
            dataChannel = DatagramChannel.open(StandardProtocolFamily.INET);
            configure(dataChannel.socket());
            if (address.isPresent())
                dataChannel.bind(new InetSocketAddress(address.get(), port.get()));
            else dataChannel.bind(new InetSocketAddress(port.get()));
        }
        return new DataChannel(
                tracingToken,
                dataChannel,
                dataChannel.getLocalAddress(),
                config.guidPrefix(),
                config.packetBufferSize());
    }

    /** Remote channel */
    public DataChannel connect(TracingToken tracingToken, Locator locator) throws IOException {
        Preconditions.isTrue(
                !locator.address().isMulticastAddress(), "Non multicast address required");
        var dataChannel =
                DatagramChannel.open(StandardProtocolFamily.INET)
                        .connect(locator.getSocketAddress());
        configure(dataChannel.socket());
        return new DataChannel(
                tracingToken,
                dataChannel,
                locator.getSocketAddress(),
                config.guidPrefix(),
                config.packetBufferSize());
    }

    private void configure(DatagramSocket socket) throws IOException {
        socket.setReceiveBufferSize(config.receiveBufferSize());
        if (socket.getReceiveBufferSize() != config.receiveBufferSize()) {
            // logger formats long numbers, so we convert them to strings
            LOGGER.warning(
                    """
            Could not set size of receive buffer, current size {0}, expected {1}. This may cause message loss.

            If running Linux try to set receive buffer manually:
            sudo sysctl -w net.core.rmem_max={1}
            sudo sysctl -w net.core.rmem_default={1}
            sudo sysctl -w net.ipv4.udp_mem={1}
            """,
                    "" + socket.getReceiveBufferSize(), "" + config.receiveBufferSize());
        }

        socket.setSendBufferSize(config.sendBufferSize());
        if (socket.getSendBufferSize() != config.sendBufferSize()) {
            // logger formats long numbers, so we convert them to strings
            LOGGER.warning(
                    """
            Could not set size of send buffer, current size {0}, expected {1}.

            If running Linux try to set receive buffer manually:
            sudo sysctl -w net.core.wmem_max={1}
            sudo sysctl -w net.core.wmem_default={1}
            sudo sysctl -w net.ipv4.udp_mem={1}
            """,
                    "" + socket.getSendBufferSize(), "" + config.sendBufferSize());
        }
    }
}
