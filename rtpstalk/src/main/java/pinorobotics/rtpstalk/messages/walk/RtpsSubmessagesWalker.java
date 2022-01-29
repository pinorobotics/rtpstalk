package pinorobotics.rtpstalk.messages.walk;

import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;

public class RtpsSubmessagesWalker {

    public void walk(RtpsMessage message, RtpsSubmessageVisitor visitor) {
        var guidPrefix = message.header.guidPrefix;
        for (var submessage : message.getSubmessages()) {
            var res = switch (submessage) {
            case Data data -> visitor.onData(guidPrefix, data);
            case AckNack ackNack -> visitor.onAckNack(guidPrefix, ackNack);
            case Heartbeat heartbeat -> visitor.onHeartbeat(guidPrefix, heartbeat);
            case InfoTimestamp infoTimestamp -> visitor.onInfoTimestamp(guidPrefix, infoTimestamp);
            default -> Result.CONTINUE;
            };
            if (res == Result.STOP)
                break;
        }

    }
}
