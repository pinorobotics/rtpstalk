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
package pinorobotics.rtpstalk.impl.spec.behavior.reader;

import static pinorobotics.rtpstalk.impl.spec.behavior.reader.ChangeFromWriterStatusKind.MISSING;
import static pinorobotics.rtpstalk.impl.spec.behavior.reader.ChangeFromWriterStatusKind.RECEIVED;

import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import pinorobotics.rtpstalk.RtpsTalkMetrics;
import pinorobotics.rtpstalk.impl.behavior.reader.WriterHeartbeatProcessor;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannel;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class WriterProxy {

    private final Meter METER = GlobalOpenTelemetry.getMeter(WriterProxy.class.getSimpleName());
    private final LongCounter LOST_CHANGES_COUNT_METER =
            METER.counterBuilder(RtpsTalkMetrics.LOST_CHANGES_COUNT_METRIC)
                    .setDescription(RtpsTalkMetrics.LOST_CHANGES_COUNT_METRIC_DESCRIPTION)
                    .build();

    private final Guid readerGuid;
    private final Guid remoteWriterGuid;
    private final List<Locator> unicastLocatorList;
    private final SortedMap<Long, ChangeFromWriterStatusKind> sortedChangesFromWriter =
            new ConcurrentSkipListMap<>();
    private final AtomicLong seqNumMax = new AtomicLong();
    private final XLogger logger;
    private final WriterHeartbeatProcessor heartbeatProcessor;
    private final TracingToken tracingToken;
    private final DataChannelFactory dataChannelFactory;
    private DataChannel dataChannel;

    public WriterProxy(
            TracingToken tracingToken,
            DataChannelFactory dataChannelFactory,
            int maxSubmessageSize,
            Guid readerGuid,
            Guid remoteWriterGuid,
            List<Locator> unicastLocatorList) {
        this.dataChannelFactory = dataChannelFactory;
        this.readerGuid = readerGuid;
        this.remoteWriterGuid = remoteWriterGuid;
        this.unicastLocatorList = List.copyOf(unicastLocatorList);
        this.tracingToken = tracingToken;
        logger = XLogger.getLogger(getClass(), tracingToken);
        heartbeatProcessor = new WriterHeartbeatProcessor(tracingToken, this, maxSubmessageSize);
    }

    public void receivedChangeSet(long seqNum) {
        if (sortedChangesFromWriter.get(seqNum) == RECEIVED) {
            logger.fine(
                    "Change with sequence number {0} already present in the cache, ignoring...",
                    seqNum);
            return;
        }
        sortedChangesFromWriter.put(seqNum, RECEIVED);
        logger.fine("New change added into the cache");
        if (seqNumMax.get() < seqNum) {
            logger.fine("Updating maximum sequence number to {0}", seqNum);
            seqNumMax.set(seqNum);
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
        return seqNumMax.get();
    }

    /**
     * List of unicast (address, port) combinations that can be used to send messages to the matched
     * Writer or Writers. The list may be empty.
     */
    public List<Locator> getUnicastLocatorList() {
        return unicastLocatorList;
    }

    public void missingChangesUpdate(long firstSN, long lastSN) {
        var iter = sortedChangesFromWriter.tailMap(firstSN).entrySet().iterator();
        var curSN = firstSN;
        var newMissing = new HashMap<Long, ChangeFromWriterStatusKind>();
        while (iter.hasNext() && curSN <= lastSN) {
            var entry = iter.next();
            if (entry.getKey() < curSN) continue;
            while (curSN < entry.getKey()) newMissing.put(curSN++, MISSING);
            if (entry.getValue() == RECEIVED) {
                curSN++;
                continue;
            }
            newMissing.put(curSN++, MISSING);
        }
        while (curSN <= lastSN) newMissing.put(curSN++, MISSING);
        sortedChangesFromWriter.putAll(newMissing);
    }

    public void lostChangesUpdate(long firstSN) {
        var lostChanges = new ArrayList<Long>();
        var iter = sortedChangesFromWriter.entrySet().iterator();
        while (iter.hasNext()) {
            var curSeqNum = iter.next().getKey();
            if (curSeqNum >= firstSN) break;
            lostChanges.add(curSeqNum);
            iter.remove();
        }
        LOST_CHANGES_COUNT_METER.add(lostChanges.size());
        if (!lostChanges.isEmpty()) {
            logger.fine(
                    "Changes from {0} to {1} are not available on the Writer anymore and are lost",
                    lostChanges.get(0), lostChanges.get(lostChanges.size() - 1));
        }
    }

    /**
     * This operation returns the subset of changes for the WriterProxy that have status {@link
     * ChangeFromWriterStatusKind#MISSING}.
     */
    public long[] missingChangesSorted() {
        return sortedChangesFromWriter.entrySet().stream()
                .filter(e -> e.getValue() == MISSING)
                .mapToLong(Entry::getKey)
                .toArray();
    }

    public WriterHeartbeatProcessor getHeartbeatProcessor() {
        return heartbeatProcessor;
    }

    public DataChannel getDataChannel() {
        if (dataChannel == null) {
            var locator = getUnicastLocatorList().get(0);
            try {
                dataChannel = dataChannelFactory.connect(tracingToken, locator);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Cannot open connection to remote writer on " + locator, e);
            }
        }
        return dataChannel;
    }
}
