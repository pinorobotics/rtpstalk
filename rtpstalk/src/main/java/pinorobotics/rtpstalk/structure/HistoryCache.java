/*
 * Copyright 2022 rtpstalk project
 * 
 * Website: https://github.com/pinorobotics/rtpstalk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pinorobotics.rtpstalk.structure;

import id.xfunction.logging.XLogger;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class HistoryCache<D extends Payload> implements Iterable<CacheChange<D>> {

    private static final XLogger LOGGER = XLogger.getLogger(HistoryCache.class);
    private long seqNumMin = SequenceNumber.MIN.value;
    private long seqNumMax = SequenceNumber.MIN.value;

    /** The list of CacheChanges contained in the HistoryCache. */
    private Map<Long, CacheChange<D>> changes = new LinkedHashMap<>();

    /** @return true if this is a new change and it was added */
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
        return seqNums.stream().map(changes::get).filter(change -> change != null);
    }
}
