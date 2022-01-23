package pinorobotics.rtpstalk.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.messages.Guid;

public class HistoryCache extends SubmissionPublisher<CacheChange> {

    /**
     * The list of CacheChanges contained in the HistoryCache.
     */
    private Map<Guid, List<CacheChange>> changes = new HashMap<>();

    public void addChange(CacheChange change) {
        List<CacheChange> writerChanges = changes.putIfAbsent(change.writerGuid(), new ArrayList<>());
        if (writerChanges == null)
            writerChanges = changes.get(change.writerGuid());
        if (writerChanges.isEmpty()) {
            writerChanges.add(change);
            return;
        }
        if (writerChanges.get(writerChanges.size() - 1).sequenceNumber().compareTo(change.sequenceNumber()) == 0) {
            return;
        }
        writerChanges.add(change);
        submit(change);
    }
}
