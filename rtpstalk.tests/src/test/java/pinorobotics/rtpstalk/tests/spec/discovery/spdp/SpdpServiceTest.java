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
package pinorobotics.rtpstalk.tests.spec.discovery.spdp;

import static pinorobotics.rtpstalk.tests.TestConstants.TEST_GUID_PREFIX;
import static pinorobotics.rtpstalk.tests.TestConstants.TEST_REMOTE_DEFAULT_UNICAST_LOCATOR;
import static pinorobotics.rtpstalk.tests.TestConstants.TEST_REMOTE_GUID_PREFIX;
import static pinorobotics.rtpstalk.tests.TestConstants.TEST_REMOTE_METATRAFFIC_UNICAST_LOCATOR;

import id.xfunction.concurrent.flow.CollectorSubscriber;
import id.xfunction.concurrent.flow.SimpleSubscriber;
import java.util.EnumSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.spec.discovery.spdp.SpdpService;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet.Endpoint;
import pinorobotics.rtpstalk.impl.spec.messages.Duration;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Header;
import pinorobotics.rtpstalk.impl.spec.messages.ProtocolId;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Timestamp;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiverFactory;
import pinorobotics.rtpstalk.tests.LogUtils;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.XAsserts;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class SpdpServiceTest {
    private static final RtpsTalkConfiguration CONFIG = TestConstants.TEST_CONFIG;
    private static final ParameterList TEST_REMOTE_SPDP_DISCOVERED_PARTICIPANT_DATA =
            createSpdpDiscoveredParticipantData(CONFIG);
    private static final RtpsMessage TEST_REMOTE_SPDP_DISCOVERED_PARTICIPANT_MESSAGE =
            new RtpsMessage(
                    new Header(
                            ProtocolId.Predefined.RTPS.getValue(),
                            ProtocolVersion.Predefined.Version_2_3.getValue(),
                            VendorId.Predefined.FASTRTPS.getValue(),
                            TEST_REMOTE_GUID_PREFIX),
                    new InfoTimestamp(new Timestamp(112233, 0)),
                    new Data(
                            EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR,
                            EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_ANNOUNCER,
                            new SequenceNumber(1),
                            new SerializedPayload(TEST_REMOTE_SPDP_DISCOVERED_PARTICIPANT_DATA)));

    private TestDataChannelFactory channelFactory;
    private SpdpService service;
    private RtpsMessageReceiverFactory receiverFactory;

    @BeforeEach
    public void setup() {
        LogUtils.setupLog();
        channelFactory = new TestDataChannelFactory(CONFIG);
        receiverFactory = new RtpsMessageReceiverFactory();
        service = new SpdpService(CONFIG, channelFactory, receiverFactory);
    }

    @AfterEach
    public void cleanup() throws Exception {
        service.close();
    }

    @Test
    public void test_publisher_happy() throws Exception {
        TestDataChannel metatrafficChannel = new TestDataChannel(TEST_GUID_PREFIX, true);
        channelFactory.addChannel(TestConstants.TEST_DEFAULT_MULTICAST_LOCATOR, metatrafficChannel);
        service.start(
                new TracingToken("test"),
                TestConstants.TEST_NETWORK_IFACE,
                TestConstants.LOOPBACK_NETWORK_IFACE,
                new SimpleSubscriber<>());
        // we expect spdp publisher startup time no longer than 100 msec
        Thread.sleep(100);
        Assertions.assertEquals(1, metatrafficChannel.getDataQueue().size());
        var message = metatrafficChannel.getDataQueue().peek().toString();
        System.out.println(message);
        XAsserts.assertMatches(getClass(), "SpdpDiscoveredParticipantData", message);
    }

    @Test
    public void test_publisher_rate() throws Exception {
        TestDataChannel metatrafficChannel = new TestDataChannel(TEST_GUID_PREFIX, true);
        channelFactory.addChannel(TestConstants.TEST_DEFAULT_MULTICAST_LOCATOR, metatrafficChannel);
        try (var service =
                new SpdpService(
                        new RtpsTalkConfiguration.Builder()
                                .spdpDiscoveredParticipantDataPublishPeriod(
                                        java.time.Duration.ofMillis(50))
                                .build(),
                        channelFactory,
                        receiverFactory)) {
            service.start(
                    new TracingToken("test"),
                    TestConstants.TEST_NETWORK_IFACE,
                    TestConstants.LOOPBACK_NETWORK_IFACE,
                    new SimpleSubscriber<>());
            Thread.sleep(160);
            var channel =
                    channelFactory.getChannels().get(TestConstants.TEST_DEFAULT_MULTICAST_LOCATOR);
            Assertions.assertNotNull(channel);
            Assertions.assertEquals(4, channel.getDataQueue().size());
        }
    }

    @Test
    public void test_new_participant_discovery() throws Exception {
        var metatrafficChannel = new TestDataChannel(TEST_GUID_PREFIX, false);
        channelFactory.addChannel(TestConstants.TEST_DEFAULT_MULTICAST_LOCATOR, metatrafficChannel);
        var collector = new CollectorSubscriber<RtpsTalkParameterListMessage>(1);
        service.start(
                new TracingToken("test"),
                TestConstants.TEST_NETWORK_IFACE,
                TestConstants.LOOPBACK_NETWORK_IFACE,
                collector);
        metatrafficChannel.addInput(TEST_REMOTE_SPDP_DISCOVERED_PARTICIPANT_MESSAGE);
        var channel =
                channelFactory.getChannels().get(TestConstants.TEST_DEFAULT_MULTICAST_LOCATOR);
        Assertions.assertNotNull(channel);
        var participants = collector.getFuture().get();
        System.out.println(participants);
        Assertions.assertEquals(1, participants.size());
        Assertions.assertEquals(
                new RtpsTalkParameterListMessage(TEST_REMOTE_SPDP_DISCOVERED_PARTICIPANT_DATA)
                        .toString(),
                participants.get(0).toString());
    }

    public static ParameterList createSpdpDiscoveredParticipantData(RtpsTalkConfiguration config) {
        var endpointSet =
                EnumSet.of(
                        Endpoint.DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR,
                        Endpoint.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_DETECTOR,
                        Endpoint.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_ANNOUNCER,
                        Endpoint.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_DETECTOR,
                        Endpoint.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_ANNOUNCER,
                        Endpoint.SECURE_PUBLICATION_READER,
                        Endpoint.PARTICIPANT_SECURE_READER,
                        Endpoint.SECURE_SUBSCRIPTION_READER,
                        Endpoint.SECURE_PARTICIPANT_MESSAGE_READER);
        var params = new ParameterList();
        params.put(
                ParameterId.PID_PROTOCOL_VERSION,
                ProtocolVersion.Predefined.Version_2_3.getValue());
        params.put(ParameterId.PID_VENDORID, VendorId.Predefined.FASTRTPS.getValue());
        params.put(
                ParameterId.PID_PARTICIPANT_GUID,
                new Guid(
                        TEST_REMOTE_GUID_PREFIX,
                        EntityId.Predefined.ENTITYID_PARTICIPANT.getValue()));
        params.put(
                ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR,
                TEST_REMOTE_METATRAFFIC_UNICAST_LOCATOR);
        params.put(ParameterId.PID_DEFAULT_UNICAST_LOCATOR, TEST_REMOTE_DEFAULT_UNICAST_LOCATOR);
        params.put(ParameterId.PID_PARTICIPANT_LEASE_DURATION, new Duration(20));
        params.put(ParameterId.PID_BUILTIN_ENDPOINT_SET, new BuiltinEndpointSet(endpointSet));
        params.put(ParameterId.PID_ENTITY_NAME, "/");
        return params;
    }
}
