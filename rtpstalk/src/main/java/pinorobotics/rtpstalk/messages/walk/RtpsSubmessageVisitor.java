package pinorobotics.rtpstalk.messages.walk;

import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;

/**
 * Visitor of submessages inside of RTPS message.
 * 
 * <p>
 * Each method accepts guidPrefix which is taken from the header of the RTPS
 * message itself.
 */
public interface RtpsSubmessageVisitor {

    default Result onAckNack(GuidPrefix guidPrefix, AckNack ackNack) {
        return Result.CONTINUE;
    }

    default Result onData(GuidPrefix guidPrefix, Data data) {
        return Result.CONTINUE;
    }

    default Result onHeartbeat(GuidPrefix guidPrefix, Heartbeat heartbeat) {
        return Result.CONTINUE;
    }

    default Result onInfoTimestamp(GuidPrefix guidPrefix, InfoTimestamp infoTimestamp) {
        return Result.CONTINUE;
    }

    default Result onInfoDestination(GuidPrefix guidPrefix, InfoDestination infoDestination) {
        return Result.CONTINUE;
    }
}
