package pinorobotics.rtpstalk.structure;

import id.xfunction.logging.XLogger;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;

public class HistoryCache<D extends Payload> implements Iterable<CacheChange<D>> {

    private static final XLogger LOGGER = XLogger.getLogger(HistoryCache.class);
    private long seqNumMin = SequenceNumber.MIN.value;
    private long seqNumMax = SequenceNumber.MIN.value;

    /**
     * The list of CacheChanges contained in the HistoryCache.
     */
    private Map<Long, CacheChange<D>> changes = new LinkedHashMap<>();

    public boolean addChange(CacheChange<D> change) {
        boolean firstChange = changes.isEmpty();
        if (changes.containsKey(change.getSequenceNumber())) {
            LOGGER.fine("Change already present in the cache, ignoring...");
            return false;
        }
        changes.put(change.getSequenceNumber(), change);
        updateSeqNums(change.getSequenceNumber(), firstChange);
        LOGGER.fine("New change added into the cache");
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
        return changes.values().iterator();
    }

    public Stream<CacheChange<D>> findAll(Collection<Long> seqNums) {
        return seqNums.stream()
                .map(changes::get)
                .filter(change -> change != null);
    }
}
