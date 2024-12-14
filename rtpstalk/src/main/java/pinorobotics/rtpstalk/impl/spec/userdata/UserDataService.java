/*
 * Copyright 2022 pinorobotics
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
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import pinorobotics.rtpstalk.impl.PublisherDetails;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.SubscriberDetails;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.RtpsReader;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
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
    private RtpsTalkConfigurationInternal config;
    private RtpsMessageReceiver receiver;
    private DataChannelFactory channelFactory;
    private Map<EntityId, RtpsReader<RtpsTalkDataMessage>> readers = new HashMap<>();
    private Map<EntityId, DataWriter> writers = new HashMap<>();
    private boolean isStarted;
    private LocalOperatingEntities operatingEntities;
    private TracingToken tracingToken;
    private DataObjectsFactory dataObjectsFactory;
    private RtpsMessageReceiverFactory receiverFactory;
    private Executor publisherExecutor;

    public UserDataService(
            RtpsTalkConfigurationInternal config,
            Executor publisherExecutor,
            DataChannelFactory channelFactory,
            DataObjectsFactory dataObjectsfactory,
            RtpsMessageReceiverFactory receiverFactory) {
        this.config = config;
        this.publisherExecutor = publisherExecutor;
        this.channelFactory = channelFactory;
        this.dataObjectsFactory = dataObjectsfactory;
        this.receiverFactory = receiverFactory;
    }

    public void subscribeToRemoteWriter(
            EntityId readerEntityId,
            List<Locator> remoteWriterDefaultUnicastLocators,
            Guid remoteWriterEndpointGuid,
            SubscriberDetails subscriberDetails) {
        Preconditions.isTrue(isStarted, "User data service is not started");
        var userSubscriber = subscriberDetails.subscriber();
        var reader = readers.get(readerEntityId);
        if (reader == null) {
            reader =
                    dataObjectsFactory.newDataReader(
                            config,
                            tracingToken,
                            publisherExecutor,
                            operatingEntities,
                            readerEntityId,
                            subscriberDetails.qosPolicy(),
                            channelFactory);
            readers.put(readerEntityId, reader);
        }
        Preconditions.equals(
                reader.getReliabilityKind(),
                subscriberDetails.qosPolicy().reliabilityKind(),
                "Subscriber already exists with reliabilityKind %s. Subscribers of different"
                        + " QoS are not allowed.");
        if (reader instanceof StatefullReliableRtpsReader<?> realiableReader)
            realiableReader.matchedWriterAdd(
                    remoteWriterEndpointGuid, remoteWriterDefaultUnicastLocators);
        if (reader.isSubscribed(userSubscriber)) {
            // this happens when there are several publishers for same topic
            // since subscriber already subscribed to DataReader we don't
            // subscribe it
        } else {
            reader.subscribe(userSubscriber);
        }
        if (!receiver.isSubscribed(reader)) receiver.subscribe(reader);
    }

    public void publish(
            EntityId writerEntityId, EntityId readerEntityId, PublisherDetails publisherDetails) {
        Preconditions.isTrue(isStarted, "User data service is not started");
        Preconditions.isTrue(
                !writers.containsKey(writerEntityId),
                "Publisher for entity id %s already exist",
                writerEntityId);
        var writer =
                dataObjectsFactory.newDataWriter(
                        config,
                        tracingToken,
                        publisherExecutor,
                        channelFactory,
                        operatingEntities,
                        writerEntityId,
                        publisherDetails.qosPolicy(),
                        publisherDetails.writerSettings());
        writers.put(writerEntityId, writer);
        publisherDetails.publisher().subscribe(writer);
        receiver.subscribe(writer.getWriterReader());
    }

    public void start(TracingToken token, RtpsNetworkInterface iface) throws IOException {
        Preconditions.isTrue(!isStarted, "Already started");
        tracingToken = token;
        logger = XLogger.getLogger(getClass(), tracingToken);
        receiver =
                receiverFactory.newRtpsMessageReceiver(
                        config.publicConfig(),
                        new TracingToken(tracingToken, "UserDataServiceReceiver"),
                        publisherExecutor);
        logger.entering("start");
        logger.fine("Starting user service on {0}", iface.getLocalDefaultUnicastLocator());
        receiver.start(iface.getDefaultUnicastChannel());
        operatingEntities = iface.getOperatingEntities();
        isStarted = true;
    }

    @Override
    public void close() {
        if (!isStarted) return;
        logger.fine("Closing");
        closeDataWriters();
        receiver.close();
        // close DataReader only after all pending changes in DataWriter were sent
        readers.values().forEach(RtpsReader::close);
    }

    public void closeDataWriters() {
        // close non builtin writers before builtin one
        Predicate<DataWriter> isBuiltin = w -> w.getGuid().entityId.isBuiltin();
        logger.fine("Closing non builtin writers");
        writers.values().stream().filter(isBuiltin.negate()).forEach(DataWriter::close);
        logger.fine("Closing builtin writers");
        writers.values().stream().filter(isBuiltin).forEach(DataWriter::close);
    }
}
