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
package pinorobotics.rtpstalk.behavior.reader;

import static pinorobotics.rtpstalk.behavior.reader.ChangeFromWriterStatusKind.*;

import id.xfunction.logging.XLogger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class WriterProxy {

    private static final XLogger LOGGER = XLogger.getLogger(WriterProxy.class);

    private Guid readerGuid;
    private Guid remoteWriterGuid;
    private List<Locator> unicastLocatorList;
    private Map<Long, ChangeFromWriterStatusKind> changesFromWriter = new LinkedHashMap<>();
    private long seqNumMax = 0;

    public WriterProxy(Guid readerGuid, Guid remoteWriterGuid, List<Locator> unicastLocatorList) {
        this.readerGuid = readerGuid;
        this.remoteWriterGuid = remoteWriterGuid;
        this.unicastLocatorList = List.copyOf(unicastLocatorList);
    }

    public void receivedChangeSet(long seqNum) {
        if (changesFromWriter.get(seqNum) == RECEIVED) {
            LOGGER.fine("Change already present in the cache, ignoring...");
            return;
        }
        changesFromWriter.put(seqNum, RECEIVED);
        LOGGER.fine("New change added into the cache");
        if (seqNumMax < seqNum) {
            LOGGER.fine("Updating maximum sequence number");
            seqNumMax = seqNum;
        }
    }

    /** Identifies the matched Writer. N/A. Configured by discovery */
    public Guid getRemoteWriterGuid() {
        return remoteWriterGuid;
    }

    /** Identifies the reader to which this Writer belongs */
    public Guid getReaderGuid() {
        return readerGuid;
    }

    /**
     * This operation returns the maximum SequenceNumber among the changesFromWriter changes in the
     * RTPS WriterProxy that are available for access by the DDS DataReader.
     */
    public long availableChangesMax() {
        return seqNumMax;
    }

    /**
     * List of unicast (address, port) combinations that can be used to send messages to the matched
     * Writer or Writers. The list may be empty.
     */
    public List<Locator> getUnicastLocatorList() {
        return unicastLocatorList;
    }

    public void missingChangesUpdate(long lastSN) {
        LongStream.rangeClosed(seqNumMax + 1, lastSN)
                .forEach(sn -> changesFromWriter.put(sn, MISSING));
    }

    public void lostChangesUpdate(long firstSN) {
        changesFromWriter =
                changesFromWriter.entrySet().stream()
                        .filter(e -> e.getKey() >= firstSN)
                        .collect(
                                Collectors.toMap(
                                        Entry::getKey,
                                        Entry::getValue,
                                        (a, b) -> b,
                                        LinkedHashMap::new));
    }

    /**
     * This operation returns the subset of changes for the WriterProxy that have status {@link
     * ChangeFromWriterStatusKind#MISSING}.
     */
    public long[] missingChanges() {
        return changesFromWriter.entrySet().stream()
                .filter(e -> e.getValue() == MISSING)
                .mapToLong(Entry::getKey)
                .toArray();
    }
}
