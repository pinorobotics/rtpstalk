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
package pinorobotics.rtpstalk.impl.behavior.reader;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import id.xfunction.text.Ellipsizer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import pinorobotics.rtpstalk.RtpsTalkMetrics;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataFragmentJoiner {

    /** Fragmented data message size includes size of metadata + user data */
    private static final int METADATA_SIZE = SerializedPayloadHeader.SIZE;

    private final Meter METER =
            GlobalOpenTelemetry.getMeter(DataFragmentJoiner.class.getSimpleName());
    private final LongHistogram JOIN_TIME_METER =
            METER.histogramBuilder(RtpsTalkMetrics.JOIN_TIME_METRIC)
                    .setDescription(RtpsTalkMetrics.JOIN_TIME_METRIC_DESCRIPTION)
                    .ofLongs()
                    .build();
    /**
     * The available size may include serializedPayloadHeader and other metadata which is not part
     * of user data itself
     */
    private int availableDataSize;

    private int availableUserdataSize;

    private Set<Long> availableFragmentsNums = new HashSet<>();
    private List<ByteBuffer> userdataFragments = new LinkedList<>();
    private DataFrag initialFragment;
    private XLogger logger;
    private Optional<Instant> startAt = Optional.empty();

    public DataFragmentJoiner(TracingToken token, DataFrag initialFragment) {
        logger = XLogger.getLogger(getClass(), token);
        this.initialFragment = initialFragment;
    }

    public boolean isEmpty() {
        return availableDataSize == 0;
    }

    public void add(DataFrag dataFrag) {
        Preconditions.isTrue(
                initialFragment.writerSN.equals(dataFrag.writerSN),
                "DataFrag belongs to the change %s and does not match current change %s ignoring"
                        + " it...",
                dataFrag.writerSN,
                initialFragment.writerSN);
        boolean shouldAdd = true;
        var fragmentStartingNum = dataFrag.fragmentStartingNum.getUnsigned();
        var fragmentsInSubmessage = dataFrag.fragmentsInSubmessage.getUnsigned();
        var receivedFragmentNums = new HashSet<Long>();
        for (var i = fragmentStartingNum; i < fragmentStartingNum + fragmentsInSubmessage; i++) {
            if (availableFragmentsNums.contains(i)) {
                logger.warning("dataFrag with number {0} already present, ignoring it...", i);
                shouldAdd = false;
                break;
            }
            receivedFragmentNums.add(i);
        }
        if (shouldAdd) {
            var serializedPayload = dataFrag.getSerializedPayload().orElse(null);
            if (serializedPayload == null) return;
            // rawData is a user data and is part of message Data
            // message Data additionally may contain metadata
            var rawData = ByteBuffer.wrap(((RawData) serializedPayload.getPayload()).getData());
            var expectedDataLen = dataFrag.fragmentSize.getUnsigned() * fragmentsInSubmessage;
            var actuaDatalLen = 0;
            if (serializedPayload.serializedPayloadHeader.isPresent())
                actuaDatalLen += SerializedPayloadHeader.SIZE;
            actuaDatalLen += rawData.capacity();
            // strip any padding
            if (actuaDatalLen > expectedDataLen) {
                actuaDatalLen = expectedDataLen;
                Preconditions.isTrue(
                        actuaDatalLen > SerializedPayloadHeader.SIZE,
                        "Data length cannot be less than metadata length");
                rawData.limit(
                        actuaDatalLen
                                - (serializedPayload.serializedPayloadHeader.isPresent()
                                        ? SerializedPayloadHeader.SIZE
                                        : 0));
            }
            if (actuaDatalLen != expectedDataLen) {
                var delta = availableDataSize + actuaDatalLen - initialFragment.dataSize;
                // check if we need to expect more fragments or if we received all data already
                if (delta > 0) {
                    // we have all data, strip padding
                    rawData.limit(rawData.capacity() - delta);
                    actuaDatalLen -= delta;
                } else if (delta < 0) {
                    // some data is still missing
                    logger.warning(
                            "DataFrag {0} length mismatch, expected {1} received {2}, ignoring"
                                    + " message",
                            fragmentStartingNum, expectedDataLen, actuaDatalLen);
                    return;
                } else {
                    // all data is present
                }
            }
            if (startAt.isEmpty() && isEmpty()) startAt = Optional.of(Instant.now());
            availableFragmentsNums.addAll(receivedFragmentNums);
            userdataFragments.add(rawData);
            availableUserdataSize += rawData.limit();
            availableDataSize += actuaDatalLen;
            logger.fine(
                    "Data message sequence number {0}: received fragments {1}, total bytes received"
                            + " {2}, total bytes expected {3}",
                    initialFragment.writerSN.value,
                    receivedFragmentNums,
                    availableDataSize,
                    initialFragment.dataSize);
        }
    }

    public boolean hasAllFragments() {
        return availableDataSize == initialFragment.dataSize;
    }

    public RtpsTalkDataMessage join() {
        Preconditions.isTrue(
                availableDataSize - availableUserdataSize == METADATA_SIZE,
                "Mismatch between available user data and metadata");
        var buf = ByteBuffer.allocate(availableUserdataSize);
        userdataFragments.forEach(buf::put);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(
                    "Data message sequence number {0}: {1}",
                    initialFragment.writerSN.value,
                    new Ellipsizer(15).ellipsizeMiddle(buf.array()));
        }
        if (startAt.isPresent()) {
            JOIN_TIME_METER.record(Duration.between(startAt.get(), Instant.now()).toMillis());
            startAt = Optional.empty();
        }
        return new RtpsTalkDataMessage(
                initialFragment
                        .inlineQos
                        .map(ParameterList::getUserParameters)
                        .map(Parameters::new),
                buf.array());
    }
}
