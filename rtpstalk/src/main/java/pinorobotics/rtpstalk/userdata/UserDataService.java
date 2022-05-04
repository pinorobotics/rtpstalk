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
package pinorobotics.rtpstalk.userdata;

import id.xfunction.Preconditions;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.RtpsNetworkInterface;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiverFactory;

/** @author lambdaprime intid@protonmail.com */
public class UserDataService implements AutoCloseable {

    private XLogger logger;
    private RtpsTalkConfiguration config;
    private RtpsMessageReceiver receiver;
    private DataChannelFactory channelFactory;
    private Map<EntityId, DataReader> readers = new HashMap<>();
    private Map<EntityId, DataWriter> writers = new HashMap<>();
    private boolean isStarted;
    private OperatingEntities operatingEntities;
    private TracingToken tracingToken;
    private DataObjectsFactory dataObjectsFactory;
    private RtpsMessageReceiverFactory receiverFactory;

    public UserDataService(
            RtpsTalkConfiguration config,
            DataChannelFactory channelFactory,
            DataObjectsFactory dataObjectsfactory,
            RtpsMessageReceiverFactory receiverFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
        this.dataObjectsFactory = dataObjectsfactory;
        this.receiverFactory = receiverFactory;
    }

    public void subscribe(EntityId entityId, Subscriber<RawData> subscriber) {
        Preconditions.isTrue(isStarted, "User data service is not started");
        var reader =
                readers.computeIfAbsent(
                        entityId,
                        eid ->
                                dataObjectsFactory.newDataReader(
                                        config, tracingToken, operatingEntities, eid));
        if (!reader.isSubscribed(subscriber)) reader.subscribe(subscriber);
        if (!receiver.isSubscribed(reader)) receiver.subscribe(reader);
    }

    public void publish(
            String topic,
            EntityId writerEntityId,
            EntityId readerEntityId,
            Publisher<RawData> publisher) {
        Preconditions.isTrue(isStarted, "User data service is not started");
        var writer =
                writers.computeIfAbsent(
                        writerEntityId,
                        eid ->
                                dataObjectsFactory.newDataWriter(
                                        config,
                                        tracingToken,
                                        channelFactory,
                                        operatingEntities,
                                        writerEntityId,
                                        topic));
        publisher.subscribe(writer);
        // to process ackNacks we create readers
        var reader =
                readers.computeIfAbsent(
                        readerEntityId,
                        eid ->
                                dataObjectsFactory.newDataReader(
                                        config, tracingToken, operatingEntities, eid));
        receiver.subscribe(reader);
    }

    public void start(TracingToken token, RtpsNetworkInterface iface) throws IOException {
        Preconditions.isTrue(!isStarted, "Already started");
        tracingToken = new TracingToken(token, iface.getName());
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
        receiver =
                receiverFactory.newRtpsMessageReceiver(
                        new TracingToken(tracingToken, "UserDataServiceReceiver"));
        logger.entering("start");
        logger.fine(
                "Starting user service on {0} using following configuration: {1}",
                iface.getName(), config);
        receiver.start(channelFactory.bind(iface.getLocalDefaultUnicastLocator()));
        operatingEntities = iface.getOperatingEntities();
        isStarted = true;
    }

    @Override
    public void close() {
        if (!isStarted) return;
        receiver.close();
        readers.values().forEach(DataReader::close);
        writers.values().forEach(DataWriter::close);
    }
}
