package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.logging.XLogger;
import id.xfunction.util.IntBitSet;
import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.transport.io.RtpsMessageWriter;

/**
 * Combines multiple heartbeats into one AckNack
 * 
 * 8.4.15.2 Efficient use of Gap and AckNack Submessages
 */
public class WriterHeartbeatProcessor {

    private static final XLogger LOGGER = XLogger.getLogger(WriterHeartbeatProcessor.class);

    private DatagramChannel dataChannel;
    private RtpsMessageWriter writer = new RtpsMessageWriter();
    private ByteBuffer buf;
    private WriterProxy writerProxy;
    private int writerCount;
    private int count;
    private Heartbeat lastHeartbeat;

    public WriterHeartbeatProcessor(WriterProxy writerProxy, int packetBufferSize) {
        this.writerProxy = writerProxy;
        buf = ByteBuffer.allocate(packetBufferSize);
    }

    /**
     * Called when new Heartbeat received
     */
    public void addHeartbeat(Heartbeat heartbeat) {
        // However, if the FinalFlag is not set, then the Reader must send an AckNack
        // message (8.3.7.5.5)
        if (heartbeat.isFinal()) {
            LOGGER.fine("Received final heartbeat, ignoring...");
            return;
        }
        if (writerCount < heartbeat.count.value) {
            writerCount = heartbeat.count.value;
            lastHeartbeat = heartbeat;
        } else {
            LOGGER.fine("Received duplicate heartbeat, ignoring...");
        }
    }

    /**
     * Ack all received heartbeats
     */
    public void ack() {
        if (lastHeartbeat == null) {
            LOGGER.fine("No new heartbeats, nothing to acknowledge...");
            return;
        }
        var writerGuid = writerProxy.getRemoteWriterGuid();
        var readerGuid = writerProxy.getReaderGuid();
        LOGGER.fine("Sending heartbeat ack for writer {0}", writerGuid);
        if (dataChannel == null) {
            var addr = writerProxy.getUnicastLocatorList().get(0).getSocketAddress();
            try {
                dataChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                        .connect(addr);
            } catch (IOException e) {
                LOGGER.warning("Cannot open connection to remote writer on {0}: {1}", addr, e.getMessage());
                return;
            }
        }
        var infoDst = new InfoDestination(writerGuid.guidPrefix);

        var ack = new AckNack(readerGuid.entityId, writerGuid.entityId, createSequenceNumberSet(lastHeartbeat),
                new Count(count++));
        var submessages = new Submessage[] { infoDst, ack };
        Header header = new Header(
                ProtocolId.Predefined.RTPS.getValue(),
                ProtocolVersion.Predefined.Version_2_3.getValue(),
                VendorId.Predefined.RTPSTALK.getValue(),
                readerGuid.guidPrefix);
        var message = new RtpsMessage(header, submessages);
        buf.rewind();
        buf.limit(buf.capacity());
        try {
            writer.writeRtpsMessage(message, buf);
            buf.limit(buf.position());
            buf.rewind();
            dataChannel.write(buf);
        } catch (Throwable e) {
            LOGGER.severe(e);
            return;
        }
        lastHeartbeat = null;
    }

    private SequenceNumberSet createSequenceNumberSet(Heartbeat heartbeat) {
        var first = heartbeat.firstSN.value;
        var last = heartbeat.lastSN.value;
        var numBits = (int) (last - first + 1);

        // Creates bitmask of missing changes between [first..last]
        var bset = new IntBitSet(numBits);
        bset.flip(0, numBits);
        var missing = numBits;
        for (var change : writerProxy.getChangesFromWriter()) {
            var n = change.getSequenceNumber().value;
            if (n < first || last < n)
                continue;
            n -= first;
            if (n >= numBits) {
                // new message, ignoring it
                continue;
            }
            bset.flip((int) n);
            missing--;
        }
        if (missing == 0) {
            return expectNextSet();
        }
        return new SequenceNumberSet(lastHeartbeat.firstSN, numBits, bset.intArray());
    }

    private SequenceNumberSet expectNextSet() {
        // all present so we expect next
        return new SequenceNumberSet(new SequenceNumber(writerProxy.availableChangesMax() + 1), 0);
    }
}
