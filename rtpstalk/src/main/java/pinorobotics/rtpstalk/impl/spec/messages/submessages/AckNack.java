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
package pinorobotics.rtpstalk.impl.spec.messages.submessages;

import id.xfunction.Preconditions;
import java.util.List;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class AckNack extends Submessage {

    /**
     * Identifies the Reader entity that acknowledges receipt of certain sequence numbers and/or
     * requests to receive certain sequence numbers.
     */
    public EntityId readerId;

    /**
     * Identifies the Writer entity that is the target of the AckNack message. This is the Writer
     * Entity that is being asked to re-send some sequence numbers or is being informed of the
     * reception of certain sequence numbers.
     */
    public EntityId writerId;

    /**
     * Communicates the state of the reader to the writer. All sequence numbers up to the one prior
     * to readerSNState.base are confirmed as received by the reader. The sequence numbers that
     * appear in the set indicate missing sequence numbers on the reader side. The ones that do not
     * appear in the set are undetermined (could be received or not).
     */
    public SequenceNumberSet readerSNState;

    /**
     * A counter that is incremented each time a new AckNack message is sent. Provides the means for
     * a Writer to detect duplicate AckNack messages that can result from the presence of redundant
     * communication paths.
     *
     * <p>RTPS specification does not say from which value it should start so presumably it can
     * start from 0. But it was observed that FastDDS ignores acknacks which has count 0. For that
     * reason we always start it from 1.
     */
    public Count count;

    public AckNack() {}

    public AckNack(
            EntityId readerId, EntityId writerId, SequenceNumberSet readerSNState, Count count) {
        Preconditions.isTrue(count.value > 0, "Count cannot be less than 1");
        this.readerId = readerId;
        this.writerId = writerId;
        this.readerSNState = readerSNState;
        this.count = count;
        submessageHeader =
                new SubmessageHeader(
                        SubmessageKind.Predefined.ACKNACK.getValue(),
                        LengthCalculator.getInstance().calculateLength(this));
    }

    @Override
    public List<String> getFlags() {
        var flags = super.getFlags();
        if (isFinal()) flags.add("Final");
        return flags;
    }

    /**
     * False indicates whether writer must respond to the AckNack message with a Heartbeat message
     */
    public boolean isFinal() {
        return (getFlagsInternal() & 2) != 0;
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] {
            "readerId", readerId,
            "writerId", writerId,
            "readerSNState", readerSNState,
            "count", count
        };
    }
}
