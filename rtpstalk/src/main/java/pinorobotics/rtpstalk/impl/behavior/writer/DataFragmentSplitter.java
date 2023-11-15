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
package pinorobotics.rtpstalk.impl.behavior.writer;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataFragmentSplitter implements Iterable<DataFrag>, Iterator<DataFrag> {

    private EntityId readerId;
    private EntityId writerId;
    private SequenceNumber writerSN;
    private Optional<ParameterList> inlineQos;
    private int fragmentSize;
    private byte[] data;
    private int currentPos, currentFragmentNum = 1;
    private int dataSize;
    private int maxSubmessageSize;
    private XLogger logger;

    public DataFragmentSplitter(
            TracingToken tracingToken,
            EntityId readerId,
            EntityId writerId,
            long writerSN,
            Optional<ParameterList> inlineQos,
            byte[] data,
            int maxSubmessageSize) {
        this.readerId = readerId;
        this.writerId = writerId;
        this.maxSubmessageSize = maxSubmessageSize;
        this.writerSN = new SequenceNumber(writerSN);
        this.inlineQos = inlineQos;
        this.data = data;
        this.dataSize = data.length + SerializedPayloadHeader.SIZE;
        LengthCalculator.getInstance().validateSubmessageSize(maxSubmessageSize);
        this.fragmentSize =
                maxSubmessageSize
                        - DataFrag.EMPTY_SUBMESSAGE_SIZE
                        - inlineQos.map(LengthCalculator.getInstance()::calculateLength).orElse(0);
        Preconditions.isLessOrEqual(
                SerializedPayloadHeader.SIZE, fragmentSize, "fragmentSize is too small");
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    @Override
    public Iterator<DataFrag> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return currentPos < data.length;
    }

    @Override
    public DataFrag next() {
        if (currentFragmentNum == 1) {
            logger.fine("Start splitting message with sequence number {0}", writerSN);
        }
        var len = fragmentSize;
        boolean hasSerializedPayloadHeader =
                DataFrag.hasSerializedPayloadHeader(currentFragmentNum);
        if (hasSerializedPayloadHeader) len -= SerializedPayloadHeader.SIZE;
        if (currentPos + len > data.length) len = data.length - currentPos;
        var fragment = Arrays.copyOfRange(data, currentPos, currentPos + len);
        currentPos += len;
        var dataFrag =
                new DataFrag(
                        readerId,
                        writerId,
                        writerSN,
                        currentFragmentNum++,
                        1,
                        fragmentSize,
                        dataSize,
                        inlineQos,
                        new SerializedPayload(new RawData(fragment), hasSerializedPayloadHeader));
        Preconditions.isLessOrEqual(
                dataFrag.getSubmessageLength(),
                maxSubmessageSize,
                "DataFrag exceeds allowed maxSubmessageSize");
        return dataFrag;
    }
}
