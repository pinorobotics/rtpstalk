package pinorobotics.rtpstalk.messages.walk;

import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;

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
}
