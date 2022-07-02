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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class HistoryCache<D extends RtpsTalkMessage> {

    private static final XLogger LOGGER = XLogger.getLogger(HistoryCache.class);

    /**
     * The list of CacheChanges contained in the HistoryCache
     *
     * <p>For Writers there is only one Writer which is adding changes to cache so there is only one
     * {@link Guid} which is {@link Guid} of the writer to which this cache belongs.
     *
     * <p>For Readers we need to keep track of changes per each matched Writer. One reason for that
     * is to tell matched Writers what changes are lost.
     */
    @RtpsSpecReference(
            paragraph = "8.2.2",
            protocolVersion = Predefined.Version_2_3,
            text = "The RTPS HistoryCache")
    private Map<Guid, WriterChanges<D>> changes = new HashMap<>();

    /**
     * Add change to the cache.
     *
     * @return true if this change was added and it has strictly increasing {@link Data#wirter} from
     *     previous changes of the same Writer
     */
    @RtpsSpecReference(
            paragraph = "8.4.2.2.1",
            protocolVersion = Predefined.Version_2_3,
            text = "Writers must not send data out-of-order")
    public boolean addChange(CacheChange<D> change) {
        var writerChanges = changes.get(change.getWriterGuid());
        if (writerChanges != null && writerChanges.containsChange(change.getSequenceNumber())) {
            LOGGER.fine("Change already present in the cache, ignoring...");
            return false;
        }
        if (writerChanges == null) {
            writerChanges = new WriterChanges<>();
            changes.put(change.getWriterGuid(), writerChanges);
        }
        var isOutOfOrder = writerChanges.getSeqNumMax() >= change.getSequenceNumber();
        writerChanges.addChange(change);
        LOGGER.fine("New change added into the cache");
        if (isOutOfOrder) {
            LOGGER.fine("Change is out-of-order");
            return false;
        }
        return true;
    }

    public Stream<CacheChange<D>> findAll(Guid writerGuid, Collection<Long> seqNums) {
        var writerChanges = changes.get(writerGuid);
        if (writerChanges == null) return Stream.of();
        return writerChanges.findAll(seqNums);
    }

    public long getSeqNumMin(Guid guid) {
        var writerChanges = changes.get(guid);
        if (writerChanges == null) return SequenceNumber.MIN.value;
        return writerChanges.getSeqNumMin();
    }

    public long getSeqNumMax(Guid guid) {
        var writerChanges = changes.get(guid);
        if (writerChanges == null) return SequenceNumber.MIN.value;
        return writerChanges.getSeqNumMax();
    }

    public int getNumberOfChanges(Guid writerGuid) {
        return Optional.ofNullable(changes.get(writerGuid))
                .map(WriterChanges::getNumberOfChanges)
                .orElse(0);
    }

    public void removeAllBelow(long oldestSeqNum) {
        changes.values().stream().forEach(changes -> changes.removeAllBelow(oldestSeqNum));
    }
}
