package pinorobotics.rtpstalk.behavior.writer;

import java.io.IOException;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.ReliabilityKind;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageSender;

/**
 * Stateless RTPS writer (best-effort reliability).
 */
public class StatelessRtpsWriter<D extends Payload> extends RtpsWriter<D> {

    private DataChannelFactory channelFactory;

    public StatelessRtpsWriter(DataChannelFactory channelFactory, Guid writerGuid, EntityId readerEntiyId) {
        super(writerGuid, readerEntiyId, ReliabilityKind.BEST_EFFORT, true);
        this.channelFactory = channelFactory;
    }

    public void readerLocatorAdd(Locator locator) throws IOException {
        var sender = new RtpsMessageSender(channelFactory.connect(locator),
                getGuid().entityId.toString());
        subscribe(sender);
    }
}
