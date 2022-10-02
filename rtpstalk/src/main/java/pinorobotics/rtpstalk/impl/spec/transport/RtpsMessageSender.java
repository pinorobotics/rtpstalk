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

import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.util.List;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.RtpsReader;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.RtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsMessageSender extends SimpleSubscriber<RtpsMessageSender.MessageBuilder>
        implements AutoCloseable {

    /**
     * {@link RtpsWriter} and {@link RtpsReader} by their nature are {@link RtpsMessage} publishers.
     * To keep them simple they don't keep track of all their subscribers. But because {@link
     * RtpsMessage} may have some fields which require explicit note about its consumer (like reader
     * entity id) we implement this interface which is populated by {@link RtpsWriter} and {@link
     * RtpsReader} and later is used by their subscribers to instantiate {@link RtpsMessage}
     * specific to their exact consumer.
     */
    public static interface MessageBuilder {

        /**
         * Reader to which this message will be sent. For example it is used by reliable writers to
         * send heartbeats for particular reader.
         */
        default GuidPrefix getReaderGuidPrefix() {
            return GuidPrefix.Predefined.GUIDPREFIX_UNKNOWN.getValue();
        }

        List<RtpsMessage> build(EntityId readerEntiyId, EntityId writerEntityId);
    }

    private final XLogger logger;
    private DataChannel dataChannel;
    private Guid remoteReader;
    private EntityId writerEntityId;

    public RtpsMessageSender(
            TracingToken tracingToken,
            DataChannel dataChannel,
            Guid remoteReader,
            EntityId writerEntityId) {
        this.dataChannel = dataChannel;
        this.remoteReader = remoteReader;
        this.writerEntityId = writerEntityId;
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    @Override
    public void onNext(MessageBuilder messageBuilder) {
        logger.entering("onNext");
        try {
            send(messageBuilder);
        } finally {
            subscription.request(1);
        }
        logger.exiting("onNext");
    }

    private void send(MessageBuilder messageBuilder) {
        try {
            var guidPrefix = messageBuilder.getReaderGuidPrefix();
            if (guidPrefix == GuidPrefix.Predefined.GUIDPREFIX_UNKNOWN.getValue()
                    || guidPrefix.equals(remoteReader.guidPrefix)) {
                messageBuilder.build(remoteReader.entityId, writerEntityId).forEach(this::send);
            } else {
                logger.fine(
                        "Not sending message since it belongs to different participant {0}",
                        guidPrefix);
            }
        } catch (Exception e) {
            logger.severe(e);
        }
    }

    @Override
    public void onComplete() {
        close();
    }

    @Override
    public void close() {
        subscription.cancel();
        dataChannel.close();
        logger.fine("Closed");
    }

    private void send(RtpsMessage message) {
        dataChannel.send(remoteReader, message);
    }

    @Override
    public void replay(MessageBuilder messageBuilder) {
        send(messageBuilder);
    }
}
