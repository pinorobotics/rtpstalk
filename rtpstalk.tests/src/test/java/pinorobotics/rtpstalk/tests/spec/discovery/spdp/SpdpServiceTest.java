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

import id.xfunction.concurrent.flow.FixedCollectorSubscriber;
import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.logging.TracingToken;
import id.xfunction.util.ImmutableMultiMap;
import id.xfunctiontests.XAsserts;
import java.util.ArrayList;
import java.util.EnumSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.spec.discovery.spdp.MetatrafficMulticastService;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet.Endpoint;
import pinorobotics.rtpstalk.impl.spec.messages.DurationT;
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
                            TestConstants.TEST_REMOTE_GUID_PREFIX),
                    new InfoTimestamp(new Timestamp(112233, 0)),
                    new Data(
                            EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR,
                            EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_ANNOUNCER,
                            new SequenceNumber(1),
                            new SerializedPayload(
                                    TEST_REMOTE_SPDP_DISCOVERED_PARTICIPANT_DATA, true)));

    private TestDataChannelFactory channelFactory;
    private MetatrafficMulticastService service;
    private RtpsMessageReceiverFactory receiverFactory;

    @BeforeEach
    public void setup() {
        LogUtils.setupLog();
        channelFactory = new TestDataChannelFactory(CONFIG);
        receiverFactory = new RtpsMessageReceiverFactory();
        service =
                new MetatrafficMulticastService(
                        TestConstants.TEST_CONFIG_INTERNAL,
                        TestConstants.TEST_PUBLISHER_EXECUTOR,
                        channelFactory,
                        receiverFactory);
    }

    @AfterEach
    public void cleanup() throws Exception {
        service.close();
    }

    @Test
    public void test_publisher_happy() throws Exception {
        TestDataChannel metatrafficChannel =
                new TestDataChannel(TestConstants.TEST_GUID_PREFIX, true);
        channelFactory.addChannel(TestConstants.TEST_DEFAULT_MULTICAST_LOCATOR, metatrafficChannel);
        service.start(
                new TracingToken("test"),
                TestConstants.TEST_NETWORK_IFACE,
                TestConstants.LOOPBACK_NETWORK_IFACE,
                new SimpleSubscriber<>());
        // we expect spdp publisher startup time no longer than 300 msec
        Thread.sleep(300);
        Assertions.assertEquals(1, metatrafficChannel.getDataQueue().size());
        var message = metatrafficChannel.getDataQueue().peek().toString();
        System.out.println(message);
        XAsserts.assertMatches(getClass(), "SpdpDiscoveredParticipantData", message);
    }

    @Test
    public void test_publisher_rate() throws Exception {
        TestDataChannel metatrafficChannel =
                new TestDataChannel(TestConstants.TEST_GUID_PREFIX, true);
        channelFactory.addChannel(TestConstants.TEST_DEFAULT_MULTICAST_LOCATOR, metatrafficChannel);
        try (var service =
                new MetatrafficMulticastService(
                        new RtpsTalkConfigurationInternal(
                                new RtpsTalkConfiguration.Builder()
                                        .spdpDiscoveredParticipantDataPublishPeriod(
                                                java.time.Duration.ofMillis(50))
                                        .build()),
                        TestConstants.TEST_PUBLISHER_EXECUTOR,
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
            // one immediately and some after
            Assertions.assertTrue(channel.getDataQueue().size() >= 3);
        }
    }

    @Test
    public void test_new_participant_discovery() throws Exception {
        var metatrafficChannel = new TestDataChannel(TestConstants.TEST_GUID_PREFIX, false);
        channelFactory.addChannel(TestConstants.TEST_DEFAULT_MULTICAST_LOCATOR, metatrafficChannel);
        var collector =
                new FixedCollectorSubscriber<>(new ArrayList<RtpsTalkParameterListMessage>(), 1);
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
        return ParameterList.ofProtocolParameters(
                ImmutableMultiMap.of(
                        ParameterId.PID_PROTOCOL_VERSION,
                        ProtocolVersion.Predefined.Version_2_3.getValue(),
                        ParameterId.PID_VENDORID,
                        VendorId.Predefined.FASTRTPS.getValue(),
                        ParameterId.PID_PARTICIPANT_GUID,
                        new Guid(
                                TestConstants.TEST_REMOTE_GUID_PREFIX,
                                EntityId.Predefined.ENTITYID_PARTICIPANT.getValue()),
                        ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR,
                        TestConstants.TEST_REMOTE_METATRAFFIC_UNICAST_LOCATOR,
                        ParameterId.PID_DEFAULT_UNICAST_LOCATOR,
                        TestConstants.TEST_REMOTE_DEFAULT_UNICAST_LOCATOR,
                        ParameterId.PID_PARTICIPANT_LEASE_DURATION,
                        new DurationT(20),
                        ParameterId.PID_BUILTIN_ENDPOINT_SET,
                        new BuiltinEndpointSet(endpointSet),
                        ParameterId.PID_ENTITY_NAME,
                        "/"));
    }
}
