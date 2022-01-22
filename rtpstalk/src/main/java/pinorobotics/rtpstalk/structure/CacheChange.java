package pinorobotics.rtpstalk.structure;

import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;

public record CacheChange(

        /**
         * The Guid that identifies the RTPS Writer that made the change
         */
        Guid writerGuid,

        /**
         * Sequence number assigned by the RTPS Writer to uniquely identify the change
         */
        SequenceNumber sequenceNumber,

        /**
         * The data value associated with the change. Depending on the kind of
         * CacheChange, there may be no associated data.
         */

        Data dataValue) {

}
