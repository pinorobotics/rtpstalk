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

import id.xfunction.XAsserts;
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
import pinorobotics.rtpstalk.userdata.UserDataService;

/**
 * Responsible for starting and managing RTPS services for each network interface specified in
 * {@link RtpsTalkConfiguration#getNetworkIfaces()}
 *
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsServiceManager implements AutoCloseable {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsServiceManager.class);
    private RtpsTalkConfiguration config;
    private boolean isStarted;
    private DataChannelFactory channelFactory;
    private List<SpdpService> spdpServices = new ArrayList<>();
    private List<SedpService> sedpServices = new ArrayList<>();
    private List<UserDataService> userServices = new ArrayList<>();

    public RtpsServiceManager(RtpsTalkConfiguration config, DataChannelFactory channelFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
    }

    public void startAll() {
        LOGGER.entering("start");
        XAsserts.assertTrue(!isStarted, "All services already started");
        LOGGER.fine("Using following configuration: {0}", config);
        for (var iface : config.networkInterfaces()) {
            var spdp = new SpdpService(config, channelFactory);
            var sedp = new SedpService(config, channelFactory);
            var userService = new UserDataService(config, channelFactory);
            try {
                spdp.start(iface);
                spdpServices.add(spdp);
                sedp.start(spdp.getParticipantsPublisher(), iface);
                sedpServices.add(sedp);
                userService.start(iface);
                userServices.add(userService);
            } catch (Exception e) {
                LOGGER.severe(
                        "Failed to start one of the RTPS services for network interface " + iface,
                        e);
            }
        }
        isStarted = true;
    }

    public void subscribe(
            String topic, String type, Subscriber<RawData> subscriber, EntityId entityId) {
        sedpServices.forEach(
                sedp -> {
                    sedp.getSubscriptionsWriter()
                            .newChange(
                                    createSubscriptionData(
                                            topic,
                                            type,
                                            entityId,
                                            sedp.getNetworkInterface()
                                                    .getLocalDefaultUnicastLocator()));
                });
        var merge = new MergeProcessor<RawData>();
        merge.subscribe(subscriber);
        userServices.forEach(
                userService -> {
                    userService.subscribe(entityId, merge.newSubscriber());
                });
    }

    public void publish(String topic, String type, Publisher<RawData> transformer) {
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
                    userService.publish(writerEntityId, readerEntityId, transformer);
                });
    }

    @Override
    public void close() throws Exception {
        if (!isStarted) return;
        spdpServices.forEach(SpdpService::close);
    }

    private ParameterList createSubscriptionData(
            String topicName, String typeName, EntityId entityId, Locator defaultUnicastLocator) {
        var params =
                List.<Entry<ParameterId, Object>>of(
                        Map.entry(ParameterId.PID_UNICAST_LOCATOR, defaultUnicastLocator),
                        Map.entry(
                                ParameterId.PID_PARTICIPANT_GUID, config.getLocalParticipantGuid()),
                        Map.entry(ParameterId.PID_TOPIC_NAME, topicName),
                        Map.entry(ParameterId.PID_TYPE_NAME, typeName),
                        Map.entry(
                                ParameterId.PID_ENDPOINT_GUID,
                                new Guid(config.guidPrefix(), entityId)),
                        Map.entry(
                                ParameterId.PID_PROTOCOL_VERSION,
                                ProtocolVersion.Predefined.Version_2_3.getValue()),
                        Map.entry(
                                ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue()));
        return new ParameterList(params);
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
