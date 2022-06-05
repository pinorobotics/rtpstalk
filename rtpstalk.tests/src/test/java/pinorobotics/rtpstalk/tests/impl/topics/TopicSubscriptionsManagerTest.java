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
package pinorobotics.rtpstalk.tests.impl.topics;

import id.xfunction.concurrent.flow.SameThreadSubmissionPublisher;
import id.xfunction.concurrent.flow.SimpleSubscriber;
import java.util.List;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.discovery.sedp.SedpBuiltinPublicationsWriter;
import pinorobotics.rtpstalk.impl.SubscriberDetails;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.impl.qos.ReliabilityKind;
import pinorobotics.rtpstalk.impl.qos.SubscriberQosPolicy;
import pinorobotics.rtpstalk.impl.topics.SedpDataFactory;
import pinorobotics.rtpstalk.impl.topics.TopicSubscriptionsManager;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.tests.LogUtils;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.XAsserts;
import pinorobotics.rtpstalk.tests.discovery.spdp.TestDataChannelFactory;
import pinorobotics.rtpstalk.tests.transport.TestRtpsMessageReceiverFactory;
import pinorobotics.rtpstalk.tests.userdata.TestDataObjectsFactory;
import pinorobotics.rtpstalk.userdata.UserDataService;

/** @author lambdaprime intid@protonmail.com */
public class TopicSubscriptionsManagerTest {

    @Test
    public void test_createSubscriptionData() throws Exception {
        LogUtils.setupLog();
        var channelFactory = new TestDataChannelFactory();
        try (var service =
                        new UserDataService(
                                TestConstants.TEST_CONFIG,
                                channelFactory,
                                new TestDataObjectsFactory(),
                                new TestRtpsMessageReceiverFactory());
                var publisher = new SameThreadSubmissionPublisher<ParameterList>()) {
            service.start(new TracingToken("test"), TestConstants.TEST_NETWORK_IFACE);
            var publicationsWriter =
                    new SedpBuiltinPublicationsWriter(
                            TestConstants.TEST_CONFIG,
                            TestConstants.TEST_TRACING_TOKEN,
                            channelFactory,
                            TestConstants.TEST_NETWORK_IFACE.getOperatingEntities());
            var manager =
                    new TopicSubscriptionsManager(
                            TestConstants.TEST_TRACING_TOKEN,
                            TestConstants.TEST_CONFIG,
                            TestConstants.TEST_NETWORK_IFACE,
                            publicationsWriter,
                            service);
            publisher.subscribe(manager);
            var topicId = new TopicId("testTopic", "testType");
            manager.addSubscriber(
                    new SubscriberDetails(
                            topicId,
                            new SubscriberQosPolicy.Builder().build(),
                            new SimpleSubscriber<>()));
            var remoteWriterPubData =
                    new SedpDataFactory(TestConstants.TEST_CONFIG)
                            .createPublicationData(
                                    topicId,
                                    EntityId.Predefined.ENTITYID_SEDP_BUILTIN_PUBLICATIONS_ANNOUNCER
                                            .getValue(),
                                    TestConstants.TEST_REMOTE_METATRAFFIC_UNICAST_LOCATOR,
                                    new PublisherQosPolicy(ReliabilityKind.RELIABLE));
            publisher.submit(remoteWriterPubData);
            var subData =
                    publicationsWriter
                            .getWriterCache()
                            .findAll(publicationsWriter.getGuid(), List.of(1L))
                            .toList()
                            .get(0)
                            .getDataValue();
            System.out.println(subData);
            XAsserts.assertEquals(getClass(), "subscriptionData", subData.toString());
        }
    }
}
