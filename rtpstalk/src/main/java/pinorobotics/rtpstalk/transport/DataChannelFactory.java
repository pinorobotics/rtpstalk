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
package pinorobotics.rtpstalk.transport;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.Locator;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class DataChannelFactory {

    private RtpsTalkConfiguration config;

    public DataChannelFactory(RtpsTalkConfiguration config) {
        this.config = config;
    }

    /** Channel bind to local port */
    public DataChannel bind(Locator locator) throws IOException {
        if (locator.address().isMulticastAddress()) {
            var ni = NetworkInterface.getByName(config.getNetworkIface());
            var dataChannel =
                    DatagramChannel.open(StandardProtocolFamily.INET)
                            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                            .bind(locator.getSocketAddress())
                            .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
            dataChannel.join(locator.address(), ni);
            return new DataChannel(
                    dataChannel,
                    locator.getSocketAddress(),
                    config.getGuidPrefix(),
                    config.getPacketBufferSize());
        } else {
            var dataChannel =
                    DatagramChannel.open(StandardProtocolFamily.INET)
                            .bind(locator.getSocketAddress());
            return new DataChannel(
                    dataChannel,
                    locator.getSocketAddress(),
                    config.getGuidPrefix(),
                    config.getPacketBufferSize());
        }
    }

    /** Remote channel */
    public DataChannel connect(Locator locator) throws IOException {
        if (locator.address().isMulticastAddress()) {
            return bind(locator);
        }
        return new DataChannel(
                DatagramChannel.open(StandardProtocolFamily.INET)
                        .connect(locator.getSocketAddress()),
                locator.getSocketAddress(),
                config.getGuidPrefix(),
                config.getPacketBufferSize());
    }
}
