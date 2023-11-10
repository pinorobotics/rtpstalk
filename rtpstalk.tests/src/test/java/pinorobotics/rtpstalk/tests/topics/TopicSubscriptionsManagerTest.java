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
package pinorobotics.rtpstalk.tests.topics;

import id.xfunction.concurrent.flow.SameThreadSubmissionPublisher;
import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.logging.TracingToken;
import java.util.List;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.SubscriberDetails;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.userdata.UserDataService;
import pinorobotics.rtpstalk.impl.topics.TopicSubscriptionsManager;
import pinorobotics.rtpstalk.tests.LogUtils;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.TestUtils;
import pinorobotics.rtpstalk.tests.XAsserts;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannelFactory;
import pinorobotics.rtpstalk.tests.spec.transport.TestRtpsMessageReceiverFactory;
import pinorobotics.rtpstalk.tests.spec.userdata.TestDataObjectsFactory;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class TopicSubscriptionsManagerTest {

    /**
     * We expect to send SubscriptionData immediately when new subscriber is registered.
     *
     * <ul>
     *   <li>create and register new subscriber with {@link TopicSubscriptionsManager}
     *   <li>test that {@link TopicSubscriptionsManager} sent proper SubscriptionData
     * </ul>
     */
    @Test
    public void test_createSubscriptionData() throws Exception {
        LogUtils.setupLog();
        var channelFactory = new TestDataChannelFactory();
        try (var service =
                        new UserDataService(
                                TestConstants.TEST_CONFIG_INTERNAL,
                                TestConstants.TEST_PUBLISHER_EXECUTOR,
                                channelFactory,
                                new TestDataObjectsFactory(),
                                new TestRtpsMessageReceiverFactory());
                var publisher = new SameThreadSubmissionPublisher<RtpsTalkParameterListMessage>()) {
            service.start(new TracingToken("test"), TestConstants.TEST_NETWORK_IFACE);
            var publicationsWriter =
                    TestUtils.newSedpPublicationsWriter(
                            channelFactory,
                            TestConstants.TEST_NETWORK_IFACE.getOperatingEntities());
            var manager =
                    new TopicSubscriptionsManager(
                            TestConstants.TEST_TRACING_TOKEN,
                            TestConstants.TEST_CONFIG_INTERNAL,
                            TestConstants.TEST_NETWORK_IFACE,
                            publicationsWriter,
                            service);
            publisher.subscribe(manager);
            var topicId = new TopicId("testTopic", "testType");
            manager.addLocalActor(
                    new SubscriberDetails(
                            topicId, new ReaderQosPolicySet(), new SimpleSubscriber<>()));
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
