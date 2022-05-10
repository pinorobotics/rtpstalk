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
package pinorobotics.rtpstalk;

import id.xfunction.Preconditions;
import id.xfunction.concurrent.flow.MergeProcessor;
import id.xfunction.logging.XLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.discovery.sedp.SedpService;
import pinorobotics.rtpstalk.discovery.spdp.SpdpService;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterfaceFactory;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.topics.TopicPublicationsManager;
import pinorobotics.rtpstalk.messages.Duration;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.ReliabilityKind;
import pinorobotics.rtpstalk.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiverFactory;
import pinorobotics.rtpstalk.userdata.DataObjectsFactory;
import pinorobotics.rtpstalk.userdata.UserDataService;

/**
 * Responsible for starting and managing RTPS services for each network interface specified in
 * {@link RtpsTalkConfiguration#getNetworkIfaces()}
 *
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsServiceManager implements AutoCloseable {

    private RtpsTalkConfiguration config;
    private boolean isStarted;
    private DataChannelFactory channelFactory;
    private List<SpdpService> spdpServices = new ArrayList<>();
    private List<SedpService> sedpServices = new ArrayList<>();
    private List<UserDataService> userServices = new ArrayList<>();
    private List<TopicPublicationsManager> topicManagers = new ArrayList<>();
    private XLogger logger;
    private RtpsMessageReceiverFactory receiverFactory;
    private RtpsNetworkInterfaceFactory networkIfaceFactory;

    public RtpsServiceManager(
            RtpsTalkConfiguration config,
            DataChannelFactory channelFactory,
            RtpsMessageReceiverFactory receiverFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
        this.receiverFactory = receiverFactory;
        networkIfaceFactory = new RtpsNetworkInterfaceFactory(config, channelFactory);
    }

    public void startAll(TracingToken tracingToken) {
        Preconditions.isTrue(!isStarted, "All services already started");
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
        logger.entering("start");
        logger.fine("Using following configuration: {0}", config);

        // looks like FASTRTPS does not support participant which runs on multiple network
        // interfaces on different ports
        // for example: lo 127.0.0.1 (7414, 7415), eth 172.17.0.2 (7412, 7413)
        // in that case it will be sending messages to lo 127.0.0.1 (7412, 7413) which is wrong
        // possibly it is expected or may be it is FASTRTPS bug but to make it work we
        // disallow support of multiple network interfaces on different ports and assign them only
        // once and for all network interfaces (see commit "Disabling multiple network interfaces
        // for SEDP and User Data endpoints")
        try {
            var rtpsIface = networkIfaceFactory.createRtpsNetworkInterface(tracingToken);
            var sedp = new SedpService(config, channelFactory, receiverFactory);
            var userService =
                    new UserDataService(
                            config, channelFactory, new DataObjectsFactory(), receiverFactory);
            var participantsPublisher = new MergeProcessor<ParameterList>();

            // Setup SEDP before SPDP to avoid race conditions when SPDP discovers participants
            // but SEDP is not subscribed to them yet (and since SPDP cache them it will
            // not notify SEDP about them anymore)
            sedp.start(tracingToken, rtpsIface);
            sedpServices.add(sedp);
            participantsPublisher.subscribe(sedp);
            startSpdp(tracingToken, rtpsIface, participantsPublisher);
            userService.start(tracingToken, rtpsIface);
            userServices.add(userService);

            var topicManager =
                    new TopicPublicationsManager(
                            tracingToken,
                            config,
                            rtpsIface,
                            sedp.getSubscriptionsWriter(),
                            userService);
            sedp.getPublicationsReader().subscribe(topicManager);
            topicManagers.add(topicManager);
        } catch (Exception e) {
            logger.severe("Failed to start one of the RTPS services", e);
        }
        isStarted = true;
    }

    private void startSpdp(
            TracingToken tracingToken,
            RtpsNetworkInterface rtpsIface,
            MergeProcessor<ParameterList> participantsPublisher)
            throws Exception {
        var networkInterfaces =
                config.networkInterface()
                        .map(List::of)
                        .orElseGet(() -> InternalUtils.getInstance().listAllNetworkInterfaces());
        for (var iface : networkInterfaces) {
            var spdp = new SpdpService(config, channelFactory, receiverFactory);
            spdp.start(tracingToken, rtpsIface, iface, participantsPublisher.newSubscriber());
            spdpServices.add(spdp);
        }
    }

    public void subscribe(String topic, String type, Subscriber<RawData> subscriber) {
        var merge = new MergeProcessor<RawData>();
        merge.subscribe(subscriber);
        topicManagers.forEach(
                topicManager -> topicManager.addSubscriber(topic, type, merge.newSubscriber()));
    }

    public void publish(String topic, String type, Publisher<RawData> publisher) {
        EntityId writerEntityId = new EntityId(config.appEntityKey(), EntityKind.WRITER_NO_KEY);
        EntityId readerEntityId = new EntityId(config.appEntityKey(), EntityKind.READER_NO_KEY);
        sedpServices.forEach(
                sedp -> {
                    sedp.getPublicationsWriter()
                            .newChange(
                                    createPublicationData(
                                            topic,
                                            type,
                                            writerEntityId,
                                            sedp.getNetworkInterface()
                                                    .getLocalDefaultUnicastLocator()));
                });
        userServices.forEach(
                userService -> {
                    userService.publish(topic, writerEntityId, readerEntityId, publisher);
                });
    }

    @Override
    public void close() {
        if (!isStarted) return;
        sedpServices.forEach(SedpService::close);
        spdpServices.forEach(SpdpService::close);
        userServices.forEach(UserDataService::close);
        logger.fine("Closed");
    }

    private ParameterList createPublicationData(
            String topicName, String typeName, EntityId entityId, Locator defaultUnicastLocator) {
        var guid = new Guid(config.guidPrefix(), entityId);
        var params =
                List.<Entry<ParameterId, Object>>of(
                        Map.entry(ParameterId.PID_UNICAST_LOCATOR, defaultUnicastLocator),
                        Map.entry(
                                ParameterId.PID_PARTICIPANT_GUID, config.getLocalParticipantGuid()),
                        Map.entry(ParameterId.PID_TOPIC_NAME, topicName),
                        Map.entry(ParameterId.PID_TYPE_NAME, typeName),
                        Map.entry(ParameterId.PID_ENDPOINT_GUID, guid),
                        Map.entry(
                                ParameterId.PID_PROTOCOL_VERSION,
                                ProtocolVersion.Predefined.Version_2_3.getValue()),
                        Map.entry(
                                ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue()),
                        Map.entry(
                                ParameterId.PID_RELIABILITY,
                                new ReliabilityQosPolicy(
                                        ReliabilityKind.RELIABLE,
                                        Duration.Predefined.ZERO.getValue())));
        return new ParameterList(params);
    }
}
