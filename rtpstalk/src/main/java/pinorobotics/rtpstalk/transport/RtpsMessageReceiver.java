package pinorobotics.rtpstalk.transport;

import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.transport.io.RtpsMessageReader;

public class RtpsMessageReceiver extends SubmissionPublisher<RtpsMessage> {
    private final XLogger logger;
    private RtpsTalkConfiguration config;
    private RtpsMessageReader reader = new RtpsMessageReader();
    private ExecutorService executor;
    private boolean isStarted;

    // TODO remove
    private DatagramChannel dataChannel;

    public RtpsMessageReceiver(RtpsTalkConfiguration config, String name) {
        this.config = config;
        executor = Executors.newSingleThreadExecutor(new NamedThreadFactory(name));
        logger = XLogger.getLogger(name);
    }

    public void start(Locator locator, boolean multicast) throws IOException {
        logger.entering("start");
        if (isStarted)
            throw new IllegalStateException("Already started");
        logger.fine("Using following configuration: {0}", config);
        dataChannel = createChannel(locator, multicast);
        executor.execute(() -> {
            var thread = Thread.currentThread();
            logger.fine("Running {0} on thread {1} with id {2}", getClass().getSimpleName(), thread.getName(),
                    thread.getId());
            while (!executor.isShutdown()) {
                try {
                    var buf = ByteBuffer.allocate(config.packetBufferSize());
                    dataChannel.receive(buf);
                    var len = buf.position();
                    buf.rewind();
                    buf.limit(len);
                    reader.readRtpsMessage(buf).ifPresent(this::submit);
                } catch (Exception e) {
                    logger.severe(e);
                }
            }
            logger.fine("Shutdown received, stopping...");
        });
        isStarted = true;
    }

    @Override
    public int submit(RtpsMessage message) {
        logger.fine("Incoming RTPS message {0}", message);
        return super.submit(message);
    }

    // TODO remove when writer ready
    public DatagramChannel getDataChannel() {
        return dataChannel;
    }

    private DatagramChannel createChannel(Locator locator, boolean multicast) throws IOException {
        DatagramChannel dataChannel = null;
        if (multicast) {
            var ni = NetworkInterface.getByName(config.networkIface());
            dataChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                    .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                    .bind(locator.getSocketAddress())
                    .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
            dataChannel.join(locator.address(), ni);
        } else {
            dataChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                    .bind(locator.getSocketAddress());
        }
        return dataChannel;
    }

}
