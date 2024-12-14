/*
 * Copyright 2022 pinorobotics
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
import id.xfunction.util.ImmutableMultiMap;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Level;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.metrics.RtpsTalkMetrics;

/**
 * User data is split on multiple fragments. These fragments are packaged in {@link DataFrag}
 * messages. One {@link DataFrag} message may contain multiple fragments. All fragments inside
 * single {@link DataFrag} are consecutive (in-order). The ordering of {@link DataFrag} messages
 * itself is not necessary consecutive (due to UDP). It means that first received {@link DataFrag}
 * may contain sequence of fragment between [11..15] and sequence of [1..5] can come later.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataFragmentJoiner {

    private final Meter METER =
            GlobalOpenTelemetry.getMeter(DataFragmentJoiner.class.getSimpleName());
    private final LongHistogram JOIN_TIME_METER =
            METER.histogramBuilder(RtpsTalkMetrics.JOIN_TIME_METRIC)
                    .setDescription(RtpsTalkMetrics.JOIN_TIME_METRIC_DESCRIPTION)
                    .ofLongs()
                    .build();
    private final LongCounter FRAGMENTS_JOIN_COMPLETE_METER =
            METER.counterBuilder(RtpsTalkMetrics.FRAGMENTED_MESSAGES_READ_COUNT_METRIC)
                    .setDescription(
                            RtpsTalkMetrics.FRAGMENTED_MESSAGES_READ_COUNT_METRIC_DESCRIPTION)
                    .build();

    private final XLogger logger;
    private final TracingToken tracingToken;

    private final long dataSequenceNumber;

    private ByteBuffer userdata;

    /** Data size includes serializedPayloadHeader + user data */
    private int availableDataSize, expectedDataSize;

    private final int expectedFragmentSize;
    private final int expectedTotalFragmentsCount;
    private final Optional<ParameterList> inlineQos;

    private final FragmentsCounter fragmentsCounter;
    private Optional<RtpsTalkDataMessage> completeDataMessage = Optional.empty();
    private Optional<Instant> startAt = Optional.empty();

    public DataFragmentJoiner(TracingToken token, DataFrag initialFragment) {
        dataSequenceNumber = initialFragment.writerSN.value;
        tracingToken = new TracingToken(token, "dataSequenceNumber" + dataSequenceNumber);
        logger = XLogger.getLogger(getClass(), tracingToken);
        expectedDataSize = initialFragment.dataSize;
        expectedFragmentSize = initialFragment.fragmentSize.getUnsigned();
        expectedTotalFragmentsCount =
                (expectedDataSize / expectedFragmentSize)
                        + ((expectedDataSize % expectedFragmentSize) != 0 ? 1 : 0);
        validate(initialFragment);
        inlineQos = initialFragment.inlineQos;
        userdata = ByteBuffer.allocate(expectedDataSize - SerializedPayloadHeader.SIZE);
        fragmentsCounter = new FragmentsCounter(expectedTotalFragmentsCount);
        logger.fine(
                "expectedDataSize={0}, expectedFragmentSize={1}, expectedTotalFragmentsCount={2},"
                        + " missingFragmentsCount={3}",
                expectedDataSize,
                expectedFragmentSize,
                expectedTotalFragmentsCount,
                fragmentsCounter.getMissingFragmentsCount());
    }

    @RtpsSpecReference(
            protocolVersion = Predefined.Version_2_3,
            paragraph = "8.3.7.3.3",
            text = "Submessage is invalid when any of the following is true:")
    private void validate(DataFrag dataFragSubmessage) {
        Preconditions.isLessOrEqual(
                dataFragSubmessage.fragmentSize.getUnsigned(),
                dataFragSubmessage.dataSize,
                tracingToken,
                "fragmentSize exceeds dataSize");
        Preconditions.isLessOrEqual(
                dataFragSubmessage.fragmentStartingNum.getUnsigned(),
                expectedTotalFragmentsCount,
                tracingToken,
                "fragmentStartingNum exceeds the total number of fragments");
        Preconditions.isLessOrEqual(
                dataFragSubmessage.fragmentStartingNum.getUnsigned()
                        + dataFragSubmessage.fragmentsInSubmessage.getUnsigned()
                        - 1,
                expectedTotalFragmentsCount,
                tracingToken,
                "fragmentStartingNum + fragmentsInSubmessage exceeds the total number of"
                        + " fragments");
    }

    private boolean isEmpty() {
        return availableDataSize == 0;
    }

    public void add(DataFrag dataFragSubmessage) {
        if (dataFragSubmessage.writerSN.value != dataSequenceNumber) {
            logger.fine(
                    "DataFrag belongs to the change {0} and does not match current change {1}"
                            + " ignoring it...",
                    dataFragSubmessage.writerSN.value, dataSequenceNumber);
            return;
        }
        if (completeDataMessage.isPresent()) return;
        validate(dataFragSubmessage);
        Preconditions.equals(
                expectedFragmentSize,
                dataFragSubmessage.fragmentSize.getUnsigned(),
                tracingToken,
                "Mismatch between expected and received size of the fragment");
        if (startAt.isEmpty() && isEmpty()) startAt = Optional.of(Instant.now());
        var fragmentStartingNum = (int) dataFragSubmessage.fragmentStartingNum.getUnsigned();
        var fragmentsInSubmessage = dataFragSubmessage.fragmentsInSubmessage.getUnsigned();
        var fragmentEndingNum = fragmentStartingNum + fragmentsInSubmessage;
        if (fragmentsCounter.isAnyFragmentPresent(fragmentStartingNum, fragmentEndingNum)) return;

        var serializedPayload = dataFragSubmessage.getSerializedPayload().orElse(null);
        if (serializedPayload == null) {
            logger.fine("Received dataFrag submessage with no serialized payload, ignoring it...");
            return;
        }
        // rawData is a user data and is part of message Data
        // message Data additionally may contain metadata (serializedPayloadHeader)
        var rawData = ByteBuffer.wrap(((RawData) serializedPayload.getPayload()).getData());
        var expectedDataLen = expectedFragmentSize * fragmentsInSubmessage;
        var actuaDatalLen = rawData.capacity();
        if (fragmentStartingNum == 1) {
            // FIRST FRAGMENT
            Preconditions.isTrue(
                    serializedPayload.serializedPayloadHeader.isPresent(),
                    tracingToken,
                    "SerializedPayloadHeader expected in the first fragment of the data");
            actuaDatalLen += SerializedPayloadHeader.SIZE;
            if (actuaDatalLen > expectedDataLen) {
                actuaDatalLen = expectedDataLen;
                rawData.limit(expectedDataLen - SerializedPayloadHeader.SIZE);
            } else
                Preconditions.equals(
                        expectedDataLen,
                        actuaDatalLen,
                        tracingToken,
                        "First fragment data underflow");
            userdata.position(0);
            userdata.put(rawData.array(), 0, rawData.limit());
            availableDataSize += actuaDatalLen;
        } else {
            Preconditions.isTrue(
                    serializedPayload.serializedPayloadHeader.isEmpty(),
                    tracingToken,
                    "SerializedPayloadHeader expected only in the first fragment of the data");
            var offset = (fragmentStartingNum - 1) * expectedFragmentSize;
            if ((fragmentEndingNum - 1) == expectedTotalFragmentsCount) {
                // LAST FRAGMENT
                if (actuaDatalLen < expectedDataLen) {
                    var delta = offset + actuaDatalLen - expectedDataSize;
                    Preconditions.isTrue(
                            delta >= 0,
                            tracingToken,
                            "Last fragment length delta underflow: %s",
                            delta);
                    // we have all data, strip padding
                    rawData.limit(rawData.capacity() - delta);
                    actuaDatalLen -= delta;
                }
            } else if (actuaDatalLen != expectedDataLen) {
                // REST FRAGMENTS
                if (actuaDatalLen > expectedDataLen) {
                    // strip padding
                    actuaDatalLen = expectedDataLen;
                    rawData.limit(expectedDataLen);
                } else
                    Preconditions.equals(
                            expectedDataLen,
                            actuaDatalLen,
                            tracingToken,
                            "Fragment data underflow");
            }
            userdata.position(offset - SerializedPayloadHeader.SIZE);
            userdata.put(rawData.array(), 0, rawData.limit());
            availableDataSize += actuaDatalLen;
        }
        fragmentsCounter.markAllFragmentsAsPresent(fragmentStartingNum, fragmentEndingNum);
        logger.fine(
                "received fragments [{0}..{1}], count of fragments missing {2}, total bytes"
                        + " received {3}, total bytes expected {4}",
                fragmentStartingNum,
                fragmentEndingNum - 1,
                fragmentsCounter.getMissingFragmentsCount(),
                availableDataSize,
                expectedDataSize);
    }

    public Optional<RtpsTalkDataMessage> join() {
        if (completeDataMessage.isPresent()) return completeDataMessage;
        if (fragmentsCounter.getMissingFragmentsCount() != 0) return Optional.empty();
        Preconditions.equals(
                expectedDataSize,
                availableDataSize,
                tracingToken,
                "Mismatch between expected and received data size");
        Preconditions.isLess(
                SerializedPayloadHeader.SIZE,
                availableDataSize,
                tracingToken,
                "Mismatch between available user data and metadata");
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("data: {0}", new Ellipsizer(15).ellipsizeMiddle(userdata.array()));
        }
        if (startAt.isPresent()) {
            JOIN_TIME_METER.record(Duration.between(startAt.get(), Instant.now()).toMillis());
            startAt = Optional.empty();
        }
        FRAGMENTS_JOIN_COMPLETE_METER.add(1);
        completeDataMessage =
                Optional.of(
                        new RtpsTalkDataMessage(
                                inlineQos
                                        .map(ParameterList::getUserParameters)
                                        .map(ImmutableMultiMap::toMap)
                                        .map(Parameters::new),
                                userdata.array()));
        return completeDataMessage;
    }
}
