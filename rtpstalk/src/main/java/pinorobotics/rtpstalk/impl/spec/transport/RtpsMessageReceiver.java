/*
 * Copyright 2022 rtpstalk project
 * 
 * Website: https://github.com/pinorobotics/rtpstalk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pinorobotics.rtpstalk.impl.spec.transport;

import id.xfunction.Preconditions;
import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;

/**
 * Receives messages from single data channel (for different endpoints) and sends them to multiple
 * subscribers (endpoint readers).
 *
 * <p>{@link RtpsMessageReceiver} Data channel it is where multiple remote writers (Participants)
 * send RTPS messages. When reader is subscribed to the data channel it is going to receive all RTPS
 * messages from it. Since one RTPS message can contain submessages which belong to different
 * readers it is reader responsibility to filter them out.
 *
 * <p>Receiver listens only one particular port.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsMessageReceiver extends SubmissionPublisher<RtpsMessage> implements AutoCloseable {
    private final XLogger logger;
    private ExecutorService executor;
    private boolean isStarted;
    private boolean isClosed;
    private DataChannel dataChannel;

    protected RtpsMessageReceiver(
            RtpsTalkConfiguration config, TracingToken tracingToken, Executor publisherExecutor) {
        super(publisherExecutor, config.publisherMaxBufferSize());
        executor =
                Executors.newSingleThreadExecutor(new NamedThreadFactory(tracingToken.toString()));
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    public void start(DataChannel dataChannel) throws IOException {
        this.dataChannel = dataChannel;
        logger.entering("start");
        Preconditions.isTrue(!isStarted, "Already started");
        executor.execute(
                () -> {
                    var thread = Thread.currentThread();
                    logger.fine(
                            "Running {0} on thread {1} with id {2}",
                            getClass().getSimpleName(), thread.getName(), thread.getId());
                    while (!executor.isShutdown()) {
                        try {
                            var message = dataChannel.receive();
                            logger.fine("Incoming RTPS message {0}", message);
                            if (!isClosed()) submit(message);
                        } catch (AsynchronousCloseException e) {
                            if (!isClosed) {
                                logger.severe(e);
                            }
                        } catch (Exception e) {
                            logger.severe(e);
                        }
                    }
                    logger.fine("Shutdown received, stopping...");
                });
        isStarted = true;
    }

    @Override
    public void subscribe(Subscriber<? super RtpsMessage> subscriber) {
        logger.fine("Subscribing {0}", subscriber);
        super.subscribe(subscriber);
    }

    @Override
    public void close() {
        if (!isStarted) return;
        if (isClosed) return;
        isClosed = true;
        logger.fine("Closing executor");
        executor.shutdown();
        logger.fine("Closing data channel");
        dataChannel.close();
        super.close();
        logger.fine("Closed");
    }
}
