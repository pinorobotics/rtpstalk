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
package pinorobotics.rtpstalk.tests.integration.thirdparty;

import id.opentelemetry.exporters.extensions.ElasticsearchMetricsExtension;
import id.xfunction.concurrent.flow.FixedCollectorSubscriber;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy.Kind;
import pinorobotics.rtpstalk.impl.topics.ActorDetails;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.qos.DurabilityType;
import pinorobotics.rtpstalk.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.qos.ReliabilityType;
import pinorobotics.rtpstalk.tests.LogExtension;
import pinorobotics.rtpstalk.tests.LogUtils;
import pinorobotics.rtpstalk.tests.TestEvents;
import pinorobotics.rtpstalk.tests.integration.thirdparty.fastdds.FastRtpsHelloWorldClient;

/**
 * @author lambdaprime intid@protonmail.com
 */
@ExtendWith({ElasticsearchMetricsExtension.class, LogExtension.class})
public class ThirdpartyClientTests {
    private FastRtpsHelloWorldClient tools;

    @BeforeEach
    public void setup() throws IOException {
        tools = new FastRtpsHelloWorldClient();
    }

    @AfterEach
    public void clean(TestInfo testInfo) {
        tools.close();
    }

    /**
     * Test that subscriber continues to receive messages when one publisher stopped and new one
     * joined. This makes sure that SEDP operates with {@link
     * DurabilityQosPolicy.Kind#TRANSIENT_LOCAL_DURABILITY_QOS}
     */
    @Test
    public void test_subscriber_when_topic_publisher_is_changed() throws Exception {
        try (var client = new RtpsTalkClient()) {
            var topicName = HelloWorldConfig.DEFAULT_TOPIC_NAME;
            var topicType = HelloWorldConfig.DEFAULT_TOPIC_TYPE;
            // register a new subscriber
            var collector =
                    new FixedCollectorSubscriber<>(new ArrayList<RtpsTalkDataMessage>(), 30) {
                        public void onNext(RtpsTalkDataMessage item) {
                            System.out.println(item);
                            super.onNext(item);
                        }
                    };
            client.subscribe(topicName, topicType, collector);
            var config = Map.of(HelloWorldExampleVariable.RunPublisher, "true");
            tools.runHelloWorldExample(config).await();
            tools.runHelloWorldExample(config).await();
            tools.runHelloWorldExample(config).await();
            collector.getFuture().get();
        }
    }

    /**
     * Once remote publisher sends N messages and local subscriber receives them they will both
     * initiate close.
     */
    @Test
    public void test_concurrent_close() throws Exception {
        try (var client = new RtpsTalkClient()) {
            var topicName = HelloWorldConfig.DEFAULT_TOPIC_NAME;
            var topicType = HelloWorldConfig.DEFAULT_TOPIC_TYPE;
            var count = 10;
            var collector =
                    new FixedCollectorSubscriber<>(new ArrayList<RtpsTalkDataMessage>(), count) {
                        public void onNext(RtpsTalkDataMessage item) {
                            System.out.println(item);
                            super.onNext(item);
                        }
                    };
            client.subscribe(topicName, topicType, collector);
            tools.runHelloWorldExample(
                    Map.of(
                            HelloWorldExampleVariable.RunPublisher,
                            "true",
                            HelloWorldExampleVariable.NumberOfMesages,
                            "" + count));
            collector.getFuture().get();
        }
        LogUtils.validateNoExceptions();
    }

    /**
     * Test that publisher replays changes in the history cache for BEST_EFFORT Subscribers. For
     * RELIABLE Subscribers it is not required as they suppose to request them through ACKNACKs
     */
    @Test
    public void test_publisher_with_best_effort_subscriber() throws Exception {
        var maxHistoryCacheSize = 200;
        try (var client =
                new RtpsTalkClient(
                        new RtpsTalkConfiguration.Builder()
                                .historyCacheMaxSize(maxHistoryCacheSize)
                                .build())) {
            var topicName = HelloWorldConfig.DEFAULT_TOPIC_NAME;
            var publisher = new SubmissionPublisher<RtpsTalkDataMessage>();
            client.publish(
                    topicName,
                    HelloWorldConfig.DEFAULT_TOPIC_TYPE,
                    new PublisherQosPolicy(
                            ReliabilityType.RELIABLE,
                            DurabilityType.TRANSIENT_LOCAL_DURABILITY_QOS),
                    publisher);
            tools.generateMessages(maxHistoryCacheSize).forEach(publisher::submit);
            publisher.close();
            var proc =
                    tools.runHelloWorldExample(
                            Map.of(
                                    HelloWorldExampleVariable.RunSubscriber,
                                    "true",
                                    HelloWorldExampleVariable.ReliabilityQosPolicyKind,
                                    "BEST_EFFORT_RELIABILITY",
                                    HelloWorldExampleVariable.NumberOfMesages,
                                    "" + maxHistoryCacheSize));
            var remoteActor =
                    TestEvents.waitForDiscoveredActor(topicName, ActorDetails.Type.Subscriber);
            Assertions.assertEquals(Kind.BEST_EFFORT, remoteActor.reliabilityKind());
            var output = proc.stdout();
            var ids = tools.extractMessageIds(output);
            Assertions.assertEquals(ids.stream().sorted().toList(), ids);
            LogUtils.validateNoExceptions();
        }
    }
}
