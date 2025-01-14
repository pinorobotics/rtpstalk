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
package pinorobotics.rtpstalk.impl.spec.messages.submessages;

import id.xfunction.Preconditions;
import java.util.List;
import java.util.Optional;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;

/**
 * This Submessage is sent from an RTPS Writer to an RTPS Reader and indicates to the RTPS Reader
 * that a range of sequence numbers is no longer relevant. The set may be a contiguous range of
 * sequence numbers or a specific set of sequence numbers.
 *
 * @author lambdaprime intid@protonmail.com
 */
public class Gap extends Submessage {
    static final List<String> STREAMED_FIELDS =
            List.of("readerId", "writerId", "gapStart", "gapList");

    /**
     * Identifies the Reader Entity that is being informed of the irrelevance of a set of sequence
     * numbers.
     */
    public EntityId readerId;

    /** Identifies the Writer Entity to which the range of sequence numbers applies. */
    public EntityId writerId;

    /** Identifies the first sequence number in the interval of irrelevant sequence numbers. */
    public SequenceNumber gapStart;

    /**
     * Serves two purposes: (1) Identifies the last sequence number in the interval of irrelevant
     * sequence numbers. (2) Identifies an additional list of sequence numbers that are irrelevant.
     */
    public SequenceNumberSet gapList;

    /**
     * Present only if the GroupInfoFlag is set in the header. Identifies the group sequence number
     * corresponding to the sample identified by gapStart.
     */
    public transient Optional<SequenceNumber> gapStartGSN = Optional.empty();

    /**
     * Present only if the GroupInfoFlag is set in the header. Identifies the end of a continuous
     * range of GSNs starting at gapStartGSN that are not available to the Reader. It shall be
     * greater than or equal to the group sequence number corresponding to the sample identified by
     * gapList.bitmapBase
     */
    public transient Optional<SequenceNumber> gapEndGSN = Optional.empty();

    public Gap() {}

    public Gap(
            EntityId readerId,
            EntityId writerId,
            SequenceNumber gapStart,
            SequenceNumberSet gapList) {
        this.readerId = readerId;
        this.writerId = writerId;
        this.gapStart = gapStart;
        this.gapList = gapList;
        submessageHeader =
                new SubmessageHeader(
                        SubmessageKind.Predefined.GAP.getValue(),
                        RtpsTalkConfiguration.ENDIANESS_BIT,
                        LengthCalculator.getInstance().calculateLength(this));
        validate();
    }

    @Override
    public List<String> getFlags() {
        var flags = super.getFlags();
        if (isGroupInfo()) flags.add("GroupInfo");
        return flags;
    }

    /**
     * Indicates the presence of additional information about the group of writers (Writer Group)
     * the sender belongs to.
     */
    public boolean isGroupInfo() {
        return (getFlagsInternal() & 8) != 0;
    }

    @Override
    @RtpsSpecReference(
            paragraph = "8.3.7.4.3",
            protocolVersion = Predefined.Version_2_3,
            text = "Validity")
    public void validate() {
        Preconditions.isLess(0, gapStart.value, "gapStart must be greater than 0");
        Preconditions.isLessOrEqual(
                gapStart.value,
                gapList.bitmapBase.value,
                "gapStart must be less or equal to gapList.base");
        gapList.validate();
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] {
            "readerId", readerId,
            "writerId", writerId,
            "gapStart", gapStart,
            "gapList", gapList,
            "gapStartGSN", gapStartGSN,
            "gapEndGSN", gapEndGSN
        };
    }
}
