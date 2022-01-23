package pinorobotics.rtpstalk.messages.walk;

import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;

public interface RtpsMessageVisitor {

    default Result onAckNack(RtpsMessage message, AckNack ackNack) {
        return Result.CONTINUE;
    }

    default Result onData(RtpsMessage message, Data data) {
        return Result.CONTINUE;
    }

    default Result onHeartbeat(RtpsMessage message, Heartbeat heartbeat) {
        return Result.CONTINUE;
    }

    default Result onInfoTimestamp(RtpsMessage message, InfoTimestamp infoTimestamp) {
        return Result.CONTINUE;
    }
}
