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
package pinorobotics.rtpstalk.transport;

import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.logging.XLogger;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.RtpsMessageBuilder;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class RtpsMessageSender extends SimpleSubscriber<RtpsMessageBuilder>
        implements AutoCloseable {

    private final XLogger logger;
    private DataChannel dataChannel;
    private Optional<InfoDestination> infoDstOpt = Optional.empty();
    private EntityId readerEntiyId;
    private EntityId writerEntityId;

    public RtpsMessageSender(
            DataChannel dataChannel,
            EntityId readerEntiyId,
            EntityId writerEntityId,
            String writerName) {
        this.dataChannel = dataChannel;
        this.readerEntiyId = readerEntiyId;
        this.writerEntityId = writerEntityId;
        logger = InternalUtils.getInstance().getLogger(getClass(), writerName);
    }

    /**
     * @param remoteReader this is used by reliable writers to send heartbeats for particular
     *     reader, for best-effort writers this can be null
     */
    public RtpsMessageSender(
            DataChannel dataChannel,
            Guid remoteReader,
            EntityId writerEntityId,
            String writerName) {
        this(dataChannel, remoteReader.entityId, writerEntityId, writerName);
        if (remoteReader != null)
            infoDstOpt = Optional.of(new InfoDestination(remoteReader.guidPrefix));
    }

    @Override
    public void onNext(RtpsMessageBuilder messageBuilder) {
        logger.entering("onNext");
        var message = messageBuilder.build(readerEntiyId, writerEntityId);
        infoDstOpt.ifPresent(
                infoDst -> {
                    if (!(message.submessages[0] instanceof Heartbeat)) return;
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

    @Override
    public void close() {
        logger.entering("close");
        subscription.cancel();
        logger.exiting("close");
    }
}
