package pinorobotics.rtpstalk.messages.walk;

import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;

public class RtpsMessageWalker {

    public void walk(RtpsMessage message, RtpsMessageVisitor visitor) {
        for (var submessage : message.getSubmessages()) {
            var res = switch (submessage) {
            case Data data -> visitor.onData(data);
            case AckNack ackNack -> visitor.onAckNack(ackNack);
            case Heartbeat heartbeat -> visitor.onHeartbeat(heartbeat);
            case InfoTimestamp infoTimestamp -> visitor.onInfoTimestamp(infoTimestamp);
            default -> Result.CONTINUE;
            };
            if (res == Result.STOP)
                break;
        }

    }
}
