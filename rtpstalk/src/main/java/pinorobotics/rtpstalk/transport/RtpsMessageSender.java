package pinorobotics.rtpstalk.transport;

import id.xfunction.concurrent.flow.XSubscriber;
import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.messages.RtpsMessage;

public class RtpsMessageSender extends XSubscriber<RtpsMessage> {

    private DataChannel dataChannel;
    private final XLogger logger;

    public RtpsMessageSender(DataChannel dataChannel, String writerName) {
        this.dataChannel = dataChannel;
        logger = XLogger.getLogger(getClass().getName() + "#" + writerName);
    }

    @Override
    public void onNext(RtpsMessage message) {
        logger.entering("onNext");
        logger.fine("Outgoing RTPS message {0}", message);
        dataChannel.send(message);
        subscription.request(1);
        logger.exiting("onNext");
    }
}
