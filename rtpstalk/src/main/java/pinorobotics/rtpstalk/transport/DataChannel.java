package pinorobotics.rtpstalk.transport;

import id.xfunction.logging.XLogger;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.transport.io.RtpsMessageReader;
import pinorobotics.rtpstalk.transport.io.RtpsMessageWriter;

public class DataChannel {
    private static final XLogger LOGGER = XLogger.getLogger(DataChannel.class);

    private RtpsMessageReader reader = new RtpsMessageReader();
    private RtpsMessageWriter writer = new RtpsMessageWriter();
    private DatagramChannel dataChannel;
    private int packetBufferSize;
    private GuidPrefix guidPrefix;

    private SocketAddress target;

    public DataChannel(DatagramChannel dataChannel, SocketAddress target, GuidPrefix guidPrefix, int packetBufferSize) {
        this.dataChannel = dataChannel;
        this.target = target;
        this.guidPrefix = guidPrefix;
        this.packetBufferSize = packetBufferSize;
    }

    public RtpsMessage receive() throws Exception {
        while (true) {
            var buf = ByteBuffer.allocate(packetBufferSize);
            dataChannel.receive(buf);
            var len = buf.position();
            buf.rewind();
            buf.limit(len);
            var messageOpt = reader.readRtpsMessage(buf);
            if (messageOpt.isEmpty())
                continue;
            var message = messageOpt.get();
            if (message.header.guidPrefix.equals(guidPrefix)) {
                LOGGER.fine("Received its own message, ignoring...");
                continue;
            }
            return message;
        }
    }

    public void send(RtpsMessage message) {
        var buf = ByteBuffer.allocate(packetBufferSize);
        buf.rewind();
        buf.limit(buf.capacity());
        try {
            writer.writeRtpsMessage(message, buf);
            buf.limit(buf.position());
            buf.rewind();
            dataChannel.send(buf, target);
        } catch (Throwable e) {
            LOGGER.severe(e);
            return;
        }
    }

}
