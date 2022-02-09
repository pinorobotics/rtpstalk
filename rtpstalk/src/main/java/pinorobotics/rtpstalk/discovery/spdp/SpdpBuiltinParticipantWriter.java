package pinorobotics.rtpstalk.discovery.spdp;

import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.XLogger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.transport.io.RtpsMessageWriter;

public class SpdpBuiltinParticipantWriter implements Runnable, AutoCloseable {

    private static final XLogger LOGGER = XLogger.getLogger(SpdpBuiltinParticipantWriter.class);
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("SpdpBuiltinParticipantWriter"));
    private RtpsMessageWriter writer = new RtpsMessageWriter();
    private DatagramChannel dc;
    private RtpsMessage spdpParticipantMessage;
    private InetAddress group;
    private RtpsTalkConfiguration config;

    public SpdpBuiltinParticipantWriter(RtpsTalkConfiguration config, DatagramChannel dc) {
        this.config = config;
        this.dc = dc;
        this.group = config.getMetatrafficMulticastLocator().address();
        spdpParticipantMessage = createEmptySpdpParticipantMessage(config);
    }

    public void start() throws Exception {
        executor.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
    }

    private RtpsMessage createEmptySpdpParticipantMessage(RtpsTalkConfiguration config) {
        var submessages = new Submessage[2];
        var header = new Header(
                ProtocolId.Predefined.RTPS.getValue(),
                ProtocolVersion.Predefined.Version_2_3.getValue(),
                VendorId.Predefined.RTPSTALK.getValue(),
                config.getGuidPrefix());
        return new RtpsMessage(header, submessages);
    }

    public void setSpdpDiscoveredParticipantData(Data data) {
        LOGGER.fine("Setting SpdpDiscoveredParticipantData {0}", data);
        spdpParticipantMessage.submessages[1] = data;
    }

    @Override
    public void run() {
        if (executor.isShutdown())
            return;
        var thread = Thread.currentThread();
        LOGGER.fine("Running SpdpBuiltinParticipantWriter on thread {0} with id {1}", thread.getName(),
                thread.getId());
        if (spdpParticipantMessage.submessages[1] == null) {
            LOGGER.fine("No SpdpDiscoveredParticipantData to send, skipping");
            return;
        }
        spdpParticipantMessage.submessages[0] = InfoTimestamp.now();
        var buf = ByteBuffer.allocate(config.getPacketBufferSize());
        try {
            writer.writeRtpsMessage(spdpParticipantMessage, buf);
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
