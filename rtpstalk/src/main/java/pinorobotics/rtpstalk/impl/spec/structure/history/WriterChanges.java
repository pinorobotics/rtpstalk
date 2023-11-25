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
package pinorobotics.rtpstalk.impl.spec.structure.history;

import id.xfunction.Preconditions;
import id.xfunction.logging.XLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class WriterChanges<D extends RtpsTalkMessage> {

    private static final XLogger LOGGER = XLogger.getLogger(WriterChanges.class);
    private SortedMap<Long, CacheChange<D>> sortedChanges = new TreeMap<>();

    /**
     * TreeMap does not guarantee constant time for {@link TreeMap#firstEntry()}, {@link
     * TreeMap#lastEntry()} (looking at Linux OpenJDK it is O(logN)) so we keep track of these
     * values manually to guarantee constant time for them
     */
    private long seqNumMin, seqNumMax;

    public WriterChanges() {
        seqNumMin = SequenceNumber.MIN.value;
        seqNumMax = SequenceNumber.MIN.value;
    }

    private void updateSeqNums(long seqNum, boolean firstChange) {
        if (firstChange) {
            LOGGER.fine("First change with sequence number {0}", seqNum);
            seqNumMin = seqNumMax = seqNum;
        } else {
            if (seqNumMin > seqNum) {
                LOGGER.fine("Updating minimum sequence number to {0}", seqNum);
                seqNumMin = seqNum;
            }
            if (seqNumMax < seqNum) {
                LOGGER.fine("Updating maximum sequence number to {0}", seqNum);
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

    public void removeAllBelow(long seqNum) {
        if (seqNum < seqNumMin) {
            return;
        }
        if (seqNum > seqNumMax) {
            seqNumMin = seqNumMax = seqNum;
            sortedChanges.clear();
            return;
        }
        var iter = sortedChanges.entrySet().iterator();
        while (iter.hasNext()) {
            var curSeqNum = iter.next().getKey();
            if (curSeqNum >= seqNum) break;
            iter.remove();
        }
        seqNumMin = seqNum - 1;
    }

    public boolean containsChange(long sequenceNumber) {
        if (sequenceNumber < seqNumMin) return false;
        if (sequenceNumber > seqNumMax) return false;
        return sortedChanges.containsKey(sequenceNumber);
    }

    public void addChange(CacheChange<D> change) {
        boolean firstChange = sortedChanges.isEmpty();
        sortedChanges.put(change.getSequenceNumber(), change);
        updateSeqNums(change.getSequenceNumber(), firstChange);
    }

    public Stream<CacheChange<D>> findAll(List<Long> seqNums) {
        if (seqNums.isEmpty()) return Stream.of();
        var i1 = seqNums.stream().sorted().iterator();
        var i2 = sortedChanges.entrySet().iterator();
        var found = new ArrayList<CacheChange<D>>(sortedChanges.size());
        while (i1.hasNext() && i2.hasNext()) {
            // unbox to primitive
            long v1 = i1.next();
            var v2 = i2.next();
            while (i1.hasNext() && v1 < v2.getKey()) {
                v1 = i1.next();
            }
            while (i2.hasNext() && v2.getKey() < v1) {
                v2 = i2.next();
            }
            if (v1 == v2.getKey()) {
                found.add(v2.getValue());
            }
        }
        return found.stream();
    }

    public int getNumberOfChanges() {
        return sortedChanges.size();
    }

    public Stream<CacheChange<D>> getAllSortedBySeqNum() {
        return sortedChanges.values().stream();
    }

    public Stream<CacheChange<D>> getAllSortedBySeqNum(long afterSeqNum) {
        Preconditions.isTrue(afterSeqNum >= 0, "cannot be negative");
        return sortedChanges.tailMap(afterSeqNum + 1).values().stream();
    }
}
