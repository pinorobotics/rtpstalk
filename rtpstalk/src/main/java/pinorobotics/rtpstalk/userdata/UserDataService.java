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

import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.RtpsNetworkInterface;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.OperatingEntities;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;

/** @author lambdaprime intid@protonmail.com */
public class UserDataService implements AutoCloseable {

    private static final XLogger LOGGER = XLogger.getLogger(UserDataService.class);
    private RtpsTalkConfiguration config;
    private RtpsMessageReceiver receiver;
    private DataChannelFactory channelFactory;
    private Map<EntityId, DataReader> readers = new HashMap<>();
    private Map<EntityId, DataWriter> writers = new HashMap<>();
    private boolean isStarted;
    private OperatingEntities operatingEntities;
    private RtpsNetworkInterface networkInterface;

    public UserDataService(RtpsTalkConfiguration config, DataChannelFactory channelFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
        receiver = new RtpsMessageReceiver("UserDataServiceReceiver");
    }

    public void subscribe(EntityId entityId, Subscriber<RawData> subscriber) {
        var reader =
                readers.computeIfAbsent(
                        entityId,
                        eid ->
                                new DataReader(
                                        config,
                                        networkInterface.getName(),
                                        operatingEntities,
                                        eid));
        reader.subscribe(subscriber);
        receiver.subscribe(reader);
    }

    public void publish(
            EntityId writerEntityId, EntityId readerEntityId, Publisher<RawData> publisher) {
        var writer =
                writers.computeIfAbsent(
                        writerEntityId,
                        eid ->
                                new DataWriter(
                                        config,
                                        networkInterface.getName(),
                                        channelFactory,
                                        operatingEntities,
                                        writerEntityId));
        publisher.subscribe(writer);
        // to process ackNacks we create readers
        var reader =
                readers.computeIfAbsent(
                        readerEntityId,
                        eid ->
                                new DataReader(
                                        config,
                                        networkInterface.getName(),
                                        operatingEntities,
                                        eid));
        receiver.subscribe(reader);
    }

    public void start(RtpsNetworkInterface iface) throws IOException {
        this.networkInterface = iface;
        LOGGER.entering("start");
        XAsserts.assertTrue(!isStarted, "Already started");
        LOGGER.fine("Starting user service using following configuration: {0}", config);
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
