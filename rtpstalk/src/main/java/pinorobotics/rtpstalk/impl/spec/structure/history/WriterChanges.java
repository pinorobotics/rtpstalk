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

import id.xfunction.logging.XLogger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Payload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;

/** @author lambdaprime intid@protonmail.com */
public class WriterChanges<D extends Payload> {

    private static final XLogger LOGGER = XLogger.getLogger(WriterChanges.class);
    private long seqNumMin;
    private long seqNumMax;
    private Map<Long, CacheChange<D>> changes = new LinkedHashMap<>();

    public WriterChanges() {
        seqNumMin = SequenceNumber.MIN.value;
        seqNumMax = SequenceNumber.MIN.value;
    }

    private void updateSeqNums(long seqNum, boolean firstChange) {
        if (firstChange) {
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

    public boolean containsChange(long sequenceNumber) {
        return changes.containsKey(sequenceNumber);
    }

    public void addChange(CacheChange<D> change) {
        boolean firstChange = changes.isEmpty();
        changes.put(change.getSequenceNumber(), change);
        updateSeqNums(change.getSequenceNumber(), firstChange);
    }

    public Stream<CacheChange<D>> findAll(Collection<Long> seqNums) {
        return seqNums.stream().map(changes::get).filter(change -> change != null);
    }
}
