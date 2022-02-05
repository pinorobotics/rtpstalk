package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.logging.XLogger;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.structure.CacheChange;

public class WriterProxy {

    private static final XLogger LOGGER = XLogger.getLogger(WriterProxy.class);

    private Guid readerGuid;
    private Guid remoteWriterGuid;
    private List<Locator> unicastLocatorList;
    private Set<CacheChange> changesFromWriter = new LinkedHashSet<>();
    private long seqNumMax = 1;

    public WriterProxy(Guid readerGuid, Guid remoteWriterGuid, List<Locator> unicastLocatorList) {
        this.readerGuid = readerGuid;
        this.remoteWriterGuid = remoteWriterGuid;
        this.unicastLocatorList = unicastLocatorList;
    }

    public void addChange(CacheChange change) {
        if (!changesFromWriter.add(change)) {
            LOGGER.fine("Change already present in the cache, ignoring...");
            return;
        }
        LOGGER.fine("New change added into the cache");
        if (seqNumMax < change.getSequenceNumber().value) {
            LOGGER.fine("Updating maximum sequence number");
            seqNumMax = change.getSequenceNumber().value;
        }
    }

    /**
     * Identifies the matched Writer. N/A. Configured by discovery
     */
    public Guid getRemoteWriterGuid() {
        return remoteWriterGuid;
    }

    /**
     * Identifies the reader to which this Writer belongs
     */
    public Guid getReaderGuid() {
        return readerGuid;
    }

    /**
     * This operation returns the maximum SequenceNumber among the changesFromWriter
     * changes in the RTPS WriterProxy that are available for access by the DDS
     * DataReader.
     */
    public long availableChangesMax() {
        return seqNumMax;
    }

    /**
     * List of CacheChange changes received or expected from the matched RTPS Writer
     */
    public Set<CacheChange> getChangesFromWriter() {
        return changesFromWriter;
    }

    /**
     * List of unicast (address, port) combinations that can be used to send
     * messages to the matched Writer or Writers. The list may be empty.
     */
    public List<Locator> getUnicastLocatorList() {
        return unicastLocatorList;
    }
}
