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
package pinorobotics.rtpstalk.impl.spec.userdata;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.function.Predicate;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiver;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiverFactory;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;

/**
 * @author lambdaprime intid@protonmail.com
 */
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

    public void subscribeToRemoteWriter(
            EntityId readerEntityId,
            List<Locator> remoteWriterDefaultUnicastLocators,
            Guid remoteWriterEndpointGuid,
            Subscriber<RtpsTalkDataMessage> subscriber) {
        Preconditions.isTrue(isStarted, "User data service is not started");
        var reader =
                readers.computeIfAbsent(
                        readerEntityId,
                        eid ->
                                dataObjectsFactory.newDataReader(
                                        config, tracingToken, operatingEntities, eid));
        reader.matchedWriterAdd(remoteWriterEndpointGuid, remoteWriterDefaultUnicastLocators);
        if (reader.isSubscribed(subscriber)) {
            // this happens when there are several publishers for same topic
            // since subscriber already subscribed to DataReader we don't
            // subscribe it
        } else {
            reader.subscribe(subscriber);
        }
        if (!receiver.isSubscribed(reader)) receiver.subscribe(reader);
    }

    public void publish(
            EntityId writerEntityId,
            EntityId readerEntityId,
            Publisher<RtpsTalkDataMessage> publisher) {
        Preconditions.isTrue(isStarted, "User data service is not started");
        Preconditions.isTrue(
                !writers.containsKey(writerEntityId),
                "Publisher for entity id " + writerEntityId + " already exist");
        var writer =
                dataObjectsFactory.newDataWriter(
                        config, tracingToken, channelFactory, operatingEntities, writerEntityId);
        writers.put(writerEntityId, writer);
        publisher.subscribe(writer);
        receiver.subscribe(writer.getWriterReader());
    }

    public void start(TracingToken token, RtpsNetworkInterface iface) throws IOException {
        Preconditions.isTrue(!isStarted, "Already started");
        tracingToken = token;
        logger = XLogger.getLogger(getClass(), tracingToken);
        receiver =
                receiverFactory.newRtpsMessageReceiver(
                        config, new TracingToken(tracingToken, "UserDataServiceReceiver"));
        logger.entering("start");
        logger.fine("Starting user service on {0}", iface.getLocalDefaultUnicastLocator());
        receiver.start(iface.getDefaultUnicastChannel());
        operatingEntities = iface.getOperatingEntities();
        isStarted = true;
    }

    @Override
    public void close() {
        if (!isStarted) return;
        // close non builtin writers before builtin one
        Predicate<DataWriter> isBuiltin = w -> w.getGuid().entityId.isBuiltin();
        writers.values().stream().filter(isBuiltin.negate()).forEach(DataWriter::close);
        writers.values().stream().filter(isBuiltin).forEach(DataWriter::close);
        receiver.close();
        // close DataReader only after all pending changes in DataWriter were sent
        readers.values().forEach(DataReader::close);
    }
}
