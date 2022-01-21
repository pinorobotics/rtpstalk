package pinorobotics.rtpstalk.messages.walk;

import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;

public interface RtpsMessageVisitor {

    default Result onAckNack(AckNack ackNack) {
        return Result.CONTINUE;
    }

    default Result onData(Data d) {
        return Result.CONTINUE;
    }

    default Result onHeartbeat(Heartbeat heartbeat) {
        return Result.CONTINUE;
    }

    default Result onInfoTimestamp(InfoTimestamp infoTimestamp) {
        return Result.CONTINUE;
    }
}
