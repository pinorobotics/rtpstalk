package pinorobotics.rtpstalk.transport;

import id.xfunction.XAsserts;
import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.messages.RtpsMessage;

/**
 * * {@link RtpsMessageReceiver} Data channel it is where multiple remote
 * writers (Participants) send RTPS messages. When reader is subscribed to the
 * data channel it is going to receive all RTPS messages from it. Since one RTPS
 * message can contain submessages which belong to different readers it is
 * reader responsibility to filter them out.
 *
 */
public class RtpsMessageReceiver extends SubmissionPublisher<RtpsMessage> {
    private final XLogger logger;
    private ExecutorService executor;
    private boolean isStarted;

    public RtpsMessageReceiver(String name) {
        executor = Executors.newSingleThreadExecutor(new NamedThreadFactory(name));
        logger = XLogger.getLogger(getClass().getName() + "#" + name);
    }

    public void start(DataChannel dataChannel) throws IOException {
        logger.entering("start");
        XAsserts.assertTrue(!isStarted, "Already started");
        executor.execute(() -> {
            var thread = Thread.currentThread();
            logger.fine("Running {0} on thread {1} with id {2}", getClass().getSimpleName(), thread.getName(),
                    thread.getId());
            while (!executor.isShutdown()) {
                try {
                    var message = dataChannel.receive();
                    logger.fine("Incoming RTPS message {0}", message);
                    submit(message);
                } catch (Exception e) {
                    logger.severe(e);
                }
            }
            logger.fine("Shutdown received, stopping...");
        });
        isStarted = true;
    }
}
