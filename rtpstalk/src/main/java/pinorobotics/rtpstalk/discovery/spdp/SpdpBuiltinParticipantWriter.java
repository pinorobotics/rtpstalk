package pinorobotics.rtpstalk.discovery.spdp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.io.RtpsMessageWriter;
import pinorobotics.rtpstalk.messages.RtpsMessage;

public class SpdpBuiltinParticipantWriter implements Runnable, AutoCloseable {

    private static final XLogger LOGGER = XLogger.getLogger(SpdpBuiltinParticipantWriter.class);
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("SPDPbuiltinParticipantWriter"));
    private RtpsMessageWriter writer = new RtpsMessageWriter();
    private DatagramChannel dc;
    private RtpsMessage data;
    private InetAddress group;
    private int packetBufferSize;

    public SpdpBuiltinParticipantWriter(DatagramChannel dc, int packetBufferSize, InetAddress group) {
        this.dc = dc;
        this.packetBufferSize = packetBufferSize;
        this.group = group;
    }

    public void start() throws Exception {
        executor.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
    }

    public void setSpdpDiscoveredParticipantData(RtpsMessage data) {
        LOGGER.fine("Setting SpdpDiscoveredParticipantData {0}", data);
        this.data = data;
    }

    @Override
    public void run() {
        if (executor.isShutdown())
            return;
        var thread = Thread.currentThread();
        LOGGER.fine("Running SPDPbuiltinParticipantWriter on thread {0} with id {1}", thread.getName(),
                thread.getId());
        if (data == null) {
            LOGGER.fine("No SpdpDiscoveredParticipantData to send, skipping");
            return;
        }
        var buf = ByteBuffer.allocate(packetBufferSize);
        try {
            writer.writeRtpsMessage(data, buf);
            buf.limit(buf.position());
            buf.rewind();
            dc.send(buf, new InetSocketAddress(group, 7400));
        } catch (Throwable e) {
            LOGGER.severe(e);
            return;
        }
        LOGGER.fine("Sent SpdpDiscoveredParticipantData");
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
    }

}
