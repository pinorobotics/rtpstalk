package pinorobotics.rtpstalk.spdp;

import static pinorobotics.rtpstalk.messages.TrafficType.*;

import pinorobotics.rtpstalk.messages.TrafficType;

public record PortNumberParameters(int DomainIdGain, int ParticipantIdGain, int PortBase,
        int d0, int d1, int d2, int d3, TrafficType trafficType) {

    public static final PortNumberParameters DEFAULT = new PortNumberParameters(250, 2, 7400, 0, 10, 1, 11, DISCOVERY);

    public int getMultiCastPort(int domainId) {
        return switch (trafficType) {
        case DISCOVERY -> PortBase + DomainIdGain * domainId + d0;
        case USER -> PortBase + DomainIdGain * domainId + d2;
        };
    }
}
