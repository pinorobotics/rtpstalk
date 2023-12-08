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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.LongStream;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.RtpsTalkMetrics;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
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

    private Guid readerGuid;
    private Guid remoteWriterGuid;
    private List<Locator> unicastLocatorList;
    private Map<Long, ChangeFromWriterStatusKind> changesFromWriter = new LinkedHashMap<>();
    private long seqNumMax = 0;
    private XLogger logger;
    private WriterHeartbeatProcessor heartbeatProcessor;
    private DataChannel dataChannel;
    private TracingToken tracingToken;
    private RtpsTalkConfiguration config;

    public WriterProxy(
            TracingToken tracingToken,
            RtpsTalkConfigurationInternal config,
            Guid readerGuid,
            Guid remoteWriterGuid,
            List<Locator> unicastLocatorList) {
        this.config = config.publicConfig();
        this.readerGuid = readerGuid;
        this.remoteWriterGuid = remoteWriterGuid;
        this.unicastLocatorList = List.copyOf(unicastLocatorList);
        this.tracingToken = tracingToken;
        logger = XLogger.getLogger(getClass(), tracingToken);
        heartbeatProcessor = new WriterHeartbeatProcessor(tracingToken, this);
    }

    public void receivedChangeSet(long seqNum) {
        if (changesFromWriter.get(seqNum) == RECEIVED) {
            logger.fine(
                    "Change with sequence number {0} already present in the cache, ignoring...",
                    seqNum);
            return;
        }
        changesFromWriter.put(seqNum, RECEIVED);
        logger.fine("New change added into the cache");
        if (seqNumMax < seqNum) {
            logger.fine("Updating maximum sequence number to {0}", seqNum);
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
        Map<Long, ChangeFromWriterStatusKind> newChangesFromWriter =
                new LinkedHashMap<>(changesFromWriter.size());
        var lostChanges = new ArrayList<Long>();
        for (var entry : changesFromWriter.entrySet()) {
            var seqNum = entry.getKey();
            if (seqNum < firstSN) {
                lostChanges.add(seqNum);
            } else {
                newChangesFromWriter.put(entry.getKey(), entry.getValue());
            }
        }
        LOST_CHANGES_COUNT_METER.add(lostChanges.size());
        if (!lostChanges.isEmpty()) {
            Collections.sort(lostChanges);
            logger.fine(
                    "Changes from {0} to {1} are not available on the Writer anymore and are lost",
                    lostChanges.get(0), lostChanges.get(lostChanges.size() - 1));
        }
        changesFromWriter = newChangesFromWriter;
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

    public WriterHeartbeatProcessor getHeartbeatProcessor() {
        return heartbeatProcessor;
    }

    public DataChannel getDataChannel() {
        if (dataChannel == null) {
            var dataChannelFactory = new DataChannelFactory(config);
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
