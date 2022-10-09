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
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class Heartbeat extends Submessage {

    /**
     * Identifies the Reader Entity that is being informed of the availability of a set of sequence
     * numbers. Can be set to {@link EntityId.Predefined#ENTITYID_UNKNOWN} to indicate all readers
     * for the writer that sent the message.
     */
    public EntityId readerId;

    /** Identifies the Writer Entity to which the range of sequence numbers applies. */
    public EntityId writerId;

    /**
     * If samples are available in the Writer, identifies the first (lowest) sequence number that is
     * available in the Writer. If no samples are available in the Writer, identifies the lowest
     * sequence number that is yet to be written by the Writer.
     */
    public SequenceNumber firstSN;

    /** Identifies the last (highest) sequence number that the Writer has ever written */
    public SequenceNumber lastSN;

    /**
     * A counter that is incremented each time a new Heartbeat message is sent. Provides the means
     * for a Reader to detect duplicate Heartbeat messages that can result from the presence of
     * redundant communication paths.
     *
     * <p>RTPS specification does not say from which value it should start so presumably it can
     * start from 0. But it was observed that FastDDS ignores heartbeats which has count 0. For that
     * reason we always start it from 1.
     */
    public Count count;

    public Heartbeat() {}

    public Heartbeat(
            EntityId readerId,
            EntityId writerId,
            SequenceNumber firstSN,
            SequenceNumber lastSN,
            Count count) {
        Preconditions.isTrue(count.value > 0, "Count cannot be less than 1");
        this.readerId = readerId;
        this.writerId = writerId;
        this.firstSN = firstSN;
        this.lastSN = lastSN;
        this.count = count;
        submessageHeader =
                new SubmessageHeader(
                        SubmessageKind.Predefined.HEARTBEAT.getValue(),
                        RtpsTalkConfiguration.ENDIANESS_BIT,
                        LengthCalculator.getInstance().calculateLength(this));
    }

    @Override
    public List<String> getFlags() {
        var flags = super.getFlags();
        if (isFinal()) flags.add("Final");
        if (isLiveliness()) flags.add("Liveliness");
        if (isGroupInfo()) flags.add("GroupInfo");
        return flags;
    }

    /**
     * Indicates whether the Reader is required to respond to the Heartbeat or if it is just an
     * advisory heartbeat. If set means the Writer does not require a response from the Reader.
     */
    public boolean isFinal() {
        return (getFlagsInternal() & 2) != 0;
    }

    /**
     * Indicates that the DDS DataWriter associated with the RTPS Writer of the message has manually
     * asserted its LIVELINESS.
     */
    public boolean isLiveliness() {
        return (getFlagsInternal() & 4) != 0;
    }

    /**
     * Indicates the presence of additional information about the group of writers (Writer Group)
     * the sender belongs to.
     */
    public boolean isGroupInfo() {
        return (getFlagsInternal() & 8) != 0;
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] {
            "readerId", readerId,
            "writerId", writerId,
            "firstSN", firstSN,
            "lastSN", lastSN,
            "count", count
        };
    }
}
