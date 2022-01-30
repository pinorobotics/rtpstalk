package pinorobotics.rtpstalk.behavior.reader;

import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.walk.Result;
import pinorobotics.rtpstalk.messages.walk.RtpsSubmessageVisitor;

public class FilterByEntityIdRtpsSubmessageVisitor implements RtpsSubmessageVisitor {

    private EntityId entityId;
    private RtpsSubmessageVisitor nextVisitor;

    public FilterByEntityIdRtpsSubmessageVisitor(EntityId entityId, RtpsSubmessageVisitor nextVisitor) {
        this.entityId = entityId;
        this.nextVisitor = nextVisitor;
    }

    @Override
    public Result onData(GuidPrefix guidPrefix, Data data) {
        if (!entityId.equals(data.readerId))
            return Result.CONTINUE;
        return nextVisitor.onData(guidPrefix, data);
    }

    @Override
    public Result onHeartbeat(GuidPrefix guidPrefix, Heartbeat heartbeat) {
        if (!entityId.equals(heartbeat.readerId))
            return Result.CONTINUE;
        return nextVisitor.onHeartbeat(guidPrefix, heartbeat);
    }
}
