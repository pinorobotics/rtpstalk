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
package pinorobotics.rtpstalk.impl;

import id.xfunction.Preconditions;
import id.xfunction.concurrent.flow.MergeProcessor;
import id.xfunction.logging.XLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.discovery.sedp.SedpService;
import pinorobotics.rtpstalk.impl.spec.discovery.spdp.SpdpService;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiverFactory;
import pinorobotics.rtpstalk.impl.spec.userdata.DataObjectsFactory;
import pinorobotics.rtpstalk.impl.spec.userdata.UserDataService;
import pinorobotics.rtpstalk.impl.topics.TopicPublicationsManager;
import pinorobotics.rtpstalk.impl.topics.TopicSubscriptionsManager;

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
    private SedpService sedpService;
    private UserDataService userService;
    private TopicSubscriptionsManager subscriptionsManager;
    private TopicPublicationsManager publicationsManager;
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
            sedpService = new SedpService(config, channelFactory, receiverFactory);
            userService =
                    new UserDataService(
                            config, channelFactory, new DataObjectsFactory(), receiverFactory);
            var participantsPublisher = new MergeProcessor<ParameterList>();

            // Setup SEDP before SPDP to avoid race conditions when SPDP discovers participants
            // but SEDP is not subscribed to them yet (and since SPDP cache them it will
            // not notify SEDP about them anymore)
            sedpService.start(tracingToken, rtpsIface);
            participantsPublisher.subscribe(sedpService);
            startSpdp(tracingToken, rtpsIface, participantsPublisher);
            userService.start(tracingToken, rtpsIface);

            subscriptionsManager =
                    new TopicSubscriptionsManager(
                            tracingToken,
                            config,
                            rtpsIface,
                            sedpService.getSubscriptionsWriter(),
                            userService);
            sedpService.getPublicationsReader().subscribe(subscriptionsManager);

            publicationsManager =
                    new TopicPublicationsManager(
                            tracingToken,
                            config,
                            rtpsIface,
                            sedpService.getPublicationsWriter(),
                            userService);
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
        subscriptionsManager.addSubscriber(
                new SubscriberDetails(new TopicId(topic, type), subscriber));
    }

    public void publish(String topic, String type, Publisher<RawData> publisher) {
        publicationsManager.addPublisher(new PublisherDetails(new TopicId(topic, type), publisher));
    }

    @Override
    public void close() {
        if (!isStarted) return;
        sedpService.close();
        spdpServices.forEach(SpdpService::close);
        userService.close();
        logger.fine("Closed");
    }
}
