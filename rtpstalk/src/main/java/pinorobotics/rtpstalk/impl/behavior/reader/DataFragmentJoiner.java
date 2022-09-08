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
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataFragmentJoiner {

    private int availableSize;

    /**
     * The availableSize may include serializedPayloadHeader and other metadata which is not part of
     * user data itself
     */
    private int userdataSize;

    private Set<Long> availableFragmentsNums = new HashSet<>();
    private List<byte[]> userdataFragments = new LinkedList<>();
    private DataFrag initialFragment;
    private XLogger logger;

    public DataFragmentJoiner(TracingToken token, DataFrag initialFragment) {
        logger = XLogger.getLogger(getClass(), token);
        this.initialFragment = initialFragment;
    }

    public boolean isEmpty() {
        return availableSize == 0;
    }

    public void add(DataFrag dataFrag) {
        Preconditions.isTrue(
                initialFragment.writerSN.equals(dataFrag.writerSN),
                "DataFrag belongs to the change "
                        + dataFrag.writerSN
                        + " and does not match current change "
                        + initialFragment.writerSN
                        + ", ignoring it...");

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
            var rawData = ((RawData) dataFrag.getSerializedPayload().getPayload()).getData();
            var expected = dataFrag.fragmentSize.getUnsigned() * fragmentsInSubmessage;
            var actualLen =
                    dataFrag.getSerializedPayload().serializedPayloadHeader != null
                            ? LengthCalculator.getInstance()
                                    .getFixedLengthInternal(SerializedPayloadHeader.class)
                            : 0;
            actualLen += rawData.length;
            if (actualLen != expected) {
                // check if it was last fragment and we have all data
                if (availableSize + actualLen != initialFragment.dataSize) {
                    logger.warning(
                            "DataFrag length mismatch, expected {0} received {1}, ignoring message",
                            expected, actualLen);
                    return;
                }
            }
            availableFragmentsNums.addAll(receivedFragmentNums);
            userdataFragments.add(rawData);
            userdataSize += rawData.length;
            availableSize += actualLen;
            logger.fine(
                    "Data message with sequence number {0}: fragments {1}, total received {2},"
                            + " total expected {3}",
                    initialFragment.writerSN.value,
                    receivedFragmentNums,
                    availableSize,
                    initialFragment.dataSize);
        }
    }

    public boolean hasAllFragments() {
        return availableSize == initialFragment.dataSize;
    }

    public RtpsTalkDataMessage join() {
        var buf = ByteBuffer.allocate(userdataSize);
        userdataFragments.forEach(buf::put);
        return new RtpsTalkDataMessage(
                initialFragment
                        .inlineQos
                        .map(ParameterList::getUserParameters)
                        .map(Parameters::new),
                buf.array());
    }
}
