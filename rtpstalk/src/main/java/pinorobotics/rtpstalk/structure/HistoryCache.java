package pinorobotics.rtpstalk.structure;

import id.xfunction.logging.XLogger;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;

public class HistoryCache extends SubmissionPublisher<CacheChange> {

    private static final XLogger LOGGER = XLogger.getLogger(HistoryCache.class);
    private SequenceNumber seqNumMin = SequenceNumber.MIN;
    private SequenceNumber seqNumMax = SequenceNumber.MAX;

    /**
     * The list of CacheChanges contained in the HistoryCache.
     */
    private Set<CacheChange> changes = new LinkedHashSet<>();

    public void addChange(CacheChange change) {
        if (!changes.add(change)) {
            LOGGER.fine("Change already present in the cache, ignoring...");
            return;
        }
        LOGGER.fine("New change added into the cache");
        var seqNum = change.getSequenceNumber();
        if (seqNumMin.compareTo(seqNum) > 0) {
            LOGGER.fine("Updating minimum sequence number");
            seqNumMin = seqNum;
        }
        if (seqNum.compareTo(seqNumMax) > 0) {
            LOGGER.fine("Updating maximum sequence number");
            seqNumMax = seqNum;
        }
        submit(change);
    }

    public SequenceNumber getSeqNumMin() {
        return seqNumMin;
    }

    public SequenceNumber getSeqNumMax() {
        return seqNumMax;
    }
}
