package pinorobotics.rtpstalk.structure;

import id.xfunction.logging.XLogger;
import java.util.LinkedHashSet;
import java.util.Set;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;

public class HistoryCache<D extends Payload> {

    private static final XLogger LOGGER = XLogger.getLogger(HistoryCache.class);
    private long seqNumMin = SequenceNumber.MIN.value;
    private long seqNumMax = SequenceNumber.MAX.value;

    /**
     * The list of CacheChanges contained in the HistoryCache.
     */
    private Set<CacheChange<D>> changes = new LinkedHashSet<>();

    public void addChange(CacheChange<D> change) {
        if (!changes.add(change)) {
            LOGGER.fine("Change already present in the cache, ignoring...");
            return;
        }
        LOGGER.fine("New change added into the cache");
        var seqNum = change.getSequenceNumber();
        if (seqNumMin > seqNum) {
            LOGGER.fine("Updating minimum sequence number");
            seqNumMin = seqNum;
        }
        if (seqNum < seqNumMax) {
            LOGGER.fine("Updating maximum sequence number");
            seqNumMax = seqNum;
        }
    }

    public long getSeqNumMin() {
        return seqNumMin;
    }

    public long getSeqNumMax() {
        return seqNumMax;
    }
}
