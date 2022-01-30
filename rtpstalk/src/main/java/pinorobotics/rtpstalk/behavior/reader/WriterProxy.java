package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.Locator;
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
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.transport.io.RtpsMessageWriter;

public class WriterProxy {

    private static final XLogger LOGGER = XLogger.getLogger(WriterProxy.class);

    /**
     * Identifies the reader to which this Writer belongs
     */
    Guid readerGuid;

    /**
     * Identifies the matched Writer. N/A. Configured by discovery
     */
    private Guid remoteWriterGuid;

    /**
     * List of unicast (address, port) combinations that can be used to send
     * messages to the matched Writer or Writers. The list may be empty.
     */
    private List<Locator> unicastLocatorList;

    /**
     * List of CacheChange changes received or expected from the matched RTPS Writer
     */
    private Set<CacheChange> changesFromWriter = new LinkedHashSet<>();

    private DatagramChannel dataChannel;
    private RtpsMessageWriter writer = new RtpsMessageWriter();
    private ByteBuffer buf;
    private SequenceNumber seqNumMax = new SequenceNumber(0);
    private int count;
    private int writerCount;

    public WriterProxy(Guid readerGuid, Guid remoteWriterGuid, int packetBufferSize, List<Locator> unicastLocatorList) {
        this.readerGuid = readerGuid;
        this.remoteWriterGuid = remoteWriterGuid;
        this.unicastLocatorList = unicastLocatorList;
        buf = ByteBuffer.allocate(packetBufferSize);
    }

    public void addChange(CacheChange change) {
        if (!changesFromWriter.add(change)) {
            LOGGER.fine("Change already present in the cache, ignoring...");
            return;
        }
        LOGGER.fine("New change added into the cache");
        if (change.getSequenceNumber().compareTo(seqNumMax) > 0) {
            LOGGER.fine("Updating maximum sequence number");
            seqNumMax = change.getSequenceNumber();
        }
    }

    public Guid getRemoteWriterGuid() {
        return remoteWriterGuid;
    }

    /**
     * This operation returns the maximum SequenceNumber among the changesFromWriter
     * changes in the RTPS WriterProxy that are available for access by the DDS
     * DataReader.
     */
    public SequenceNumber availableChangesMax() {
        return seqNumMax;
    }

    public void onHeartbeat(Heartbeat heartbeat) {
        if (writerCount < heartbeat.count.value) {
            writerCount = heartbeat.count.value;
            // LOGGER.fine("Sending heartbeat ack for writer {0}", remoteWriterGuid);
            ack();
        } else {
            LOGGER.fine("Received duplicate heartbeat, ignoring...");
        }
    }

    private void ack() {
        if (dataChannel == null) {
            var addr = unicastLocatorList.get(0).getSocketAddress();
            try {
                dataChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                        .connect(addr);
            } catch (IOException e) {
                LOGGER.warning("Cannot open connection to remote writer on {0}: {1}", addr, e.getMessage());
                return;
            }
        }
        var infoDst = new InfoDestination(remoteWriterGuid.guidPrefix);
        var ack = new AckNack(readerGuid.entityId, remoteWriterGuid.entityId,
                new SequenceNumberSet(new SequenceNumber(availableChangesMax().value + 1)), new Count(count++));
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
    }

}
