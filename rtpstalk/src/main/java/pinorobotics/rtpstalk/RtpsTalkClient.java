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
import id.xfunction.concurrent.flow.TransformProcessor;
import id.xfunction.logging.XLogger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.discovery.sedp.SedpService;
import pinorobotics.rtpstalk.discovery.spdp.SpdpService;
import pinorobotics.rtpstalk.exceptions.RtpsTalkException;
import pinorobotics.rtpstalk.messages.Duration;
import pinorobotics.rtpstalk.messages.Guid;
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

/** @author lambdaprime intid@protonmail.com */
public class RtpsTalkClient {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsTalkClient.class);
    private RtpsTalkConfiguration config;
    private DataChannelFactory channelFactory;
    private SpdpService spdp;
    private SedpService sedp;
    private UserDataService userService;
    private boolean isStarted;

    public RtpsTalkClient() {
        this(new RtpsTalkConfiguration.Builder().build());
    }

    public RtpsTalkClient(RtpsTalkConfiguration config) {
        this.config = config;
        channelFactory = new DataChannelFactory(config);
        spdp = new SpdpService(config, channelFactory);
        sedp = new SedpService(config, channelFactory);
        userService = new UserDataService(config, channelFactory);
    }

    public void subscribe(String topic, String type, Subscriber<byte[]> subscriber) {
        if (!isStarted) {
            start();
        }
        var entityId = new EntityId(config.appEntityKey(), EntityKind.READER_NO_KEY);
        sedp.getSubscriptionsWriter().newChange(createSubscriptionData(topic, type, entityId));
        var transformer = new TransformProcessor<>(RawData::getData);
        transformer.subscribe(subscriber);
        userService.subscribe(entityId, transformer);
    }

    public void publish(String topic, String type, Publisher<byte[]> publisher) {
        if (!isStarted) {
            start();
        }
        EntityId writerEntityId = new EntityId(config.appEntityKey(), EntityKind.WRITER_NO_KEY);
        EntityId readerEntityId = new EntityId(config.appEntityKey(), EntityKind.READER_NO_KEY);
        sedp.getPublicationsWriter().newChange(createPublicationData(topic, type, writerEntityId));
        var transformer = new TransformProcessor<byte[], RawData>(RawData::new);
        userService.publish(writerEntityId, readerEntityId, transformer);
        publisher.subscribe(transformer);
    }

    private void start() {
        LOGGER.entering("start");
        XAsserts.assertTrue(!isStarted, "Already started");
        LOGGER.fine("Using following configuration: {0}", config);
        try {
            sedp.start(spdp.getReader());
            spdp.start();
            userService.start();
        } catch (Exception e) {
            throw new RtpsTalkException(e);
        }
        isStarted = true;
        LOGGER.exiting("start");
    }

    private ParameterList createSubscriptionData(
            String topicName, String typeName, EntityId entityId) {
        var params =
                List.<Entry<ParameterId, Object>>of(
                        Map.entry(ParameterId.PID_UNICAST_LOCATOR, config.defaultUnicastLocator()),
                        Map.entry(
                                ParameterId.PID_PARTICIPANT_GUID,
                                new Guid(
                                        config.guidPrefix(),
                                        EntityId.Predefined.ENTITYID_PARTICIPANT.getValue())),
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
            String topicName, String typeName, EntityId entityId) {
        var guid = new Guid(config.guidPrefix(), entityId);
        var params =
                List.<Entry<ParameterId, Object>>of(
                        Map.entry(ParameterId.PID_UNICAST_LOCATOR, config.defaultUnicastLocator()),
                        Map.entry(
                                ParameterId.PID_PARTICIPANT_GUID,
                                new Guid(
                                        config.guidPrefix(),
                                        EntityId.Predefined.ENTITYID_PARTICIPANT.getValue())),
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
