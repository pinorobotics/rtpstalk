package pinorobotics.rtpstalk.transport;

import id.xfunction.concurrent.flow.XSubscriber;
import id.xfunction.logging.XLogger;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;

public class RtpsMessageSender extends XSubscriber<RtpsMessage> {

    private final XLogger logger;
    private DataChannel dataChannel;
    private Optional<InfoDestination> infoDstOpt = Optional.empty();

    public RtpsMessageSender(DataChannel dataChannel, String writerName) {
        this(dataChannel, writerName, null);
    }

    /**
     * @param remoteReader this is used by reliable writers to send heartbeats for
     *                     particular reader, for best-effort writers this can be
     *                     null
     */
    public RtpsMessageSender(DataChannel dataChannel, String writerName, GuidPrefix remoteReader) {
        this.dataChannel = dataChannel;
        logger = InternalUtils.getInstance().getLogger(getClass(), writerName);
        if (remoteReader != null)
            infoDstOpt = Optional.of(new InfoDestination(remoteReader));
    }

    @Override
    public void onNext(RtpsMessage message) {
        logger.entering("onNext");
        infoDstOpt.ifPresent(infoDst -> {
            if (!(message.submessages[0] instanceof Heartbeat))
                return;
            logger.fine("This is Heartbeat message, including InfoDestination into it");
            var submessages = new Submessage[2];
            submessages[0] = infoDst;
            submessages[1] = message.submessages[0];
            message.submessages = submessages;
        });
        logger.fine("Outgoing RTPS message {0}", message);
        dataChannel.send(message);
        subscription.request(1);
        logger.exiting("onNext");
    }
}
