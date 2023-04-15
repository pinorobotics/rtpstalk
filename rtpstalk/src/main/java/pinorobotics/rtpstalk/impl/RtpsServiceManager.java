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
import id.xfunction.lang.XRE;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.TimeUnit;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.qos.WriterQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.discovery.sedp.MetatrafficUnicastService;
import pinorobotics.rtpstalk.impl.spec.discovery.spdp.MetatrafficMulticastService;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiverFactory;
import pinorobotics.rtpstalk.impl.spec.userdata.DataObjectsFactory;
import pinorobotics.rtpstalk.impl.spec.userdata.UserDataService;
import pinorobotics.rtpstalk.impl.topics.TopicPublicationsManager;
import pinorobotics.rtpstalk.impl.topics.TopicSubscriptionsManager;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.qos.SubscriberQosPolicy;

/**
 * Responsible for starting and managing RTPS services for each network interface specified in
 * {@link RtpsTalkConfiguration#networkInterface()}
 *
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsServiceManager implements AutoCloseable {

    private RtpsTalkConfigurationInternal config;
    private boolean isStarted;
    private DataChannelFactory channelFactory;
    private List<MetatrafficMulticastService> spdpServices = new ArrayList<>();
    private MetatrafficUnicastService sedpService;
    private UserDataService userService;
    private TopicSubscriptionsManager subscriptionsManager;
    private TopicPublicationsManager publicationsManager;
    private XLogger logger;
    private RtpsMessageReceiverFactory receiverFactory;
    private RtpsNetworkInterfaceFactory networkIfaceFactory;
    private ExecutorService publisherExecutor;

    public RtpsServiceManager(
            RtpsTalkConfigurationInternal config,
            DataChannelFactory channelFactory,
            RtpsMessageReceiverFactory receiverFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
        this.receiverFactory = receiverFactory;
        networkIfaceFactory =
                new RtpsNetworkInterfaceFactory(config.publicConfig(), channelFactory);
    }

    public void startAll(TracingToken tracingToken) {
        Preconditions.isTrue(!isStarted, "All services already started");
        logger = XLogger.getLogger(getClass(), tracingToken);
        logger.entering("start");
        logger.fine("Using following configuration: {0}", config);

        publisherExecutor =
                config.publicConfig()
                        .publisherExecutor()
                        .orElseGet(RtpsTalkConfiguration.Builder.DEFAULT_PUBLISHER_EXECUTOR);

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
            sedpService =
                    new MetatrafficUnicastService(
                            config, publisherExecutor, channelFactory, receiverFactory);
            userService =
                    new UserDataService(
                            config,
                            publisherExecutor,
                            channelFactory,
                            new DataObjectsFactory(),
                            receiverFactory);

            // Setup SEDP before SPDP to avoid race conditions when SPDP discovers participants
            // but SEDP is not subscribed to them yet (and since SPDP cache them it will
            // not notify SEDP about them anymore)
            sedpService.start(tracingToken, rtpsIface);
            startSpdp(tracingToken, rtpsIface);
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
            sedpService.getSubscriptionsReader().subscribe(publicationsManager);
        } catch (Exception e) {
            logger.severe("Failed to start one of the RTPS services", e);
        }
        isStarted = true;
    }

    private void startSpdp(TracingToken tracingToken, RtpsNetworkInterface rtpsIface)
            throws Exception {
        var networkInterfaces =
                config.publicConfig()
                        .networkInterface()
                        .map(List::of)
                        .orElseGet(() -> InternalUtils.getInstance().listAllNetworkInterfaces());
        Preconditions.isTrue(!networkInterfaces.isEmpty(), "No network interfaces found");
        logger.fine("Starting SPDP on following network interfaces {0}", networkInterfaces);
        for (var iface : networkInterfaces) {
            try {
                var spdp =
                        new MetatrafficMulticastService(
                                config, publisherExecutor, channelFactory, receiverFactory);
                spdp.start(tracingToken, rtpsIface, iface, sedpService.newSedpConfigurator());
                spdpServices.add(spdp);
            } catch (Exception e) {
                logger.severe("Error starting SPDP on network interface " + iface, e);
            }
        }
    }

    public EntityId subscribe(
            String topic,
            String type,
            SubscriberQosPolicy policy,
            Subscriber<RtpsTalkDataMessage> subscriber) {
        return subscriptionsManager.addLocalActor(
                new SubscriberDetails(
                        new TopicId(topic, type), new ReaderQosPolicySet(policy), subscriber));
    }

    public void publish(
            String topic,
            String type,
            PublisherQosPolicy policy,
            Publisher<RtpsTalkDataMessage> publisher) {
        publicationsManager.addLocalActor(
                new PublisherDetails(
                        new TopicId(topic, type), new WriterQosPolicySet(policy), publisher));
    }

    @Override
    public void close() {
        if (!isStarted) return;
        subscriptionsManager.close();
        // announce that all local publications/subscriptions are disposed
        // we need to keep SPDP still open at that point in case if any of the remote
        // Participants initiate close operation as well
        sedpService.close();
        spdpServices.forEach(MetatrafficMulticastService::close);
        // close DataReader/DataWriter only after we announce to all Participants
        // that publications/subscriptions are disposed. Otherwise if we close
        // userdata port early, Participants may get an exceptions trying to send us anything
        userService.close();
        // if publisherExecutor is set it is managed by the user, otherwise it
        // is managed by us and we should shut it down
        if (config.publicConfig().publisherExecutor().isEmpty()) {
            logger.fine("Closing publisherExecutor");
            publisherExecutor.shutdown();
            try {
                if (!publisherExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
                    throw new XRE("Timeout waiting publisher executor service to shutdown");
                }
            } catch (InterruptedException e) {
                logger.severe("Error on close", e);
            }
        } else {
            logger.fine("Not closing publisherExecutor as it is managed by the user");
        }
        logger.fine("Closed");
    }
}
