package pinorobotics.rtpstalk.structure;

import id.xfunction.logging.XLogger;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;

public class HistoryCache<D extends Payload> implements Iterable<CacheChange<D>> {

    private static final XLogger LOGGER = XLogger.getLogger(HistoryCache.class);
    private long seqNumMin = SequenceNumber.MIN.value;
    private long seqNumMax = SequenceNumber.MIN.value;

    /**
     * The list of CacheChanges contained in the HistoryCache.
     */
    private Set<CacheChange<D>> changes = new LinkedHashSet<>();

    public boolean addChange(CacheChange<D> change) {
        boolean firstChange = changes.isEmpty();
        if (!changes.add(change)) {
            LOGGER.fine("Change already present in the cache, ignoring...");
            return false;
        }
        LOGGER.fine("New change added into the cache");
        updateSeqNums(change.getSequenceNumber(), firstChange);
        return true;
    }

    private void updateSeqNums(long seqNum, boolean firstChange) {
        if (firstChange) {
            seqNumMin = seqNumMax = seqNum;
        } else {
            if (seqNumMin > seqNum) {
                LOGGER.fine("Updating minimum sequence number");
                seqNumMin = seqNum;
            }
            if (seqNumMax < seqNum) {
                LOGGER.fine("Updating maximum sequence number");
                seqNumMax = seqNum;
            }
        }
    }

    public long getSeqNumMin() {
        return seqNumMin;
    }

    public long getSeqNumMax() {
        return seqNumMax;
    }

    @Override
    public Iterator<CacheChange<D>> iterator() {
        return changes.iterator();
    }
}
