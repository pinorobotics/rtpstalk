package pinorobotics.rtpstalk.transport;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.Locator;

public class DataChannelFactory {

    private RtpsTalkConfiguration config;

    public DataChannelFactory(RtpsTalkConfiguration config) {
        this.config = config;
    }

    /**
     * Channel bind to local port
     */
    public DataChannel bind(Locator locator) throws IOException {
        if (locator.address().isMulticastAddress()) {
            var ni = NetworkInterface.getByName(config.getNetworkIface());
            var dataChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                    .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                    .bind(locator.getSocketAddress())
                    .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
            dataChannel.join(locator.address(), ni);
            return new DataChannel(dataChannel, config.getGuidPrefix(), config.getPacketBufferSize());
        } else {
            var dataChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                    .bind(locator.getSocketAddress());
            return new DataChannel(dataChannel, config.getGuidPrefix(), config.getPacketBufferSize());
        }
    }

    /**
     * Remote channel
     */
    public DataChannel connect(Locator locator) throws IOException {
        return new DataChannel(DatagramChannel.open(StandardProtocolFamily.INET)
                .connect(locator.getSocketAddress()), config.getGuidPrefix(), config.getPacketBufferSize());
    }
}
