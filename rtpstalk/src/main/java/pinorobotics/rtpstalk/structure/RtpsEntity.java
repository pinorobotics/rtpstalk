package pinorobotics.rtpstalk.structure;

import pinorobotics.rtpstalk.messages.Guid;

/**
 * RTPS Entity is the base class for all RTPS entities and maps to a DDS Entity
 */
public interface RtpsEntity {

    /**
     * Globally and uniquely identifies the RTPS Entity within the DDS domain
     */
    Guid getGuid();
}
