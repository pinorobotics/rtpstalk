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
package pinorobotics.rtpstalk.tests.integration;

import id.pubsubtests.PubSubClientTests;
import id.xfunction.concurrent.flow.FixedCollectorSubscriber;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy.Kind;
import pinorobotics.rtpstalk.impl.topics.ActorDetails;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.qos.DurabilityType;
import pinorobotics.rtpstalk.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.qos.ReliabilityType;
import pinorobotics.rtpstalk.tests.LogUtils;
import pinorobotics.rtpstalk.tests.TestEvents;
import pinorobotics.rtpstalk.tests.integration.fastdds.FastRtpsHelloWorldExample;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsTalkClientTests extends PubSubClientTests {

    private FastRtpsHelloWorldExample tools;

    static Stream<TestCase> dataProvider() {
        return Stream.of(new TestCase(RtpsTalkTestPubSubClient::new));
    }

    @BeforeEach
    public void setup() throws IOException {
        LogUtils.setupLog();
        tools = new FastRtpsHelloWorldExample();
    }

    @AfterEach
    public void clean() {
        tools.close();
    }

    @Test
    public void test_publish_single_message() throws Exception {
        try (var subscriberClient = new RtpsTalkClient();
                var publisherClient = new RtpsTalkClient(); ) {
            var topicName = "HelloWorldTopic";
            var topicType = "HelloWorld";
            var publisher = new SubmissionPublisher<RtpsTalkDataMessage>();
            var data = new RtpsTalkDataMessage("1234");
            publisherClient.publish(
                    topicName,
                    topicType,
                    new PublisherQosPolicy(
                            ReliabilityType.RELIABLE,
                            DurabilityType.TRANSIENT_LOCAL_DURABILITY_QOS),
                    publisher);
            var collector =
                    new FixedCollectorSubscriber<>(new ArrayList<RtpsTalkDataMessage>(), 1) {
                        public void onNext(RtpsTalkDataMessage item) {
                            System.out.println(item);
                            super.onNext(item);
                        }
                    };
            subscriberClient.subscribe(topicName, topicType, collector);
            publisher.submit(data);
            Assertions.assertEquals(data.toString(), collector.getFuture().get().get(0).toString());
        }
    }

    @Test
    public void test_inlineQos() throws Exception {
        try (var subscriberClient = new RtpsTalkClient();
                var publisherClient = new RtpsTalkClient()) {
            var topicName = "HelloWorldTopic";
            var topicType = "HelloWorld";
            // register a new subscriber
            var collector =
                    new FixedCollectorSubscriber<>(new ArrayList<RtpsTalkDataMessage>(), 3) {
                        public void onNext(RtpsTalkDataMessage item) {
                            System.out.println(item);
                            super.onNext(item);
                        }
                    };
            subscriberClient.subscribe(topicName, topicType, collector);
            var publisher = new SubmissionPublisher<RtpsTalkDataMessage>();
            publisherClient.publish(topicName, topicType, publisher);
            RtpsTalkDataMessage[] expected = {
                new RtpsTalkDataMessage("abcdefgh"),
                new RtpsTalkDataMessage(new Parameters(Map.of((short) 11, "1234".getBytes()))),
                new RtpsTalkDataMessage(
                        new Parameters(Map.of((short) 12, "5678".getBytes())), "abcd".getBytes()),
            };
            Arrays.stream(expected).forEach(publisher::submit);
            var actual = collector.getFuture().get();
            Assertions.assertEquals(Arrays.toString(expected), actual.toString());
        }
    }

    /**
     * Test that subscriber continues to receive messages when one publisher stopped and new one
     * joined. This makes sure that SEDP operates with {@link
     * DurabilityQosPolicy.Kind#TRANSIENT_LOCAL_DURABILITY_QOS}
     */
    @Test
    public void test_subscriber_when_topic_publisher_is_changed() throws Exception {
        try (var client = new RtpsTalkClient()) {
            var topicName = "HelloWorldTopic";
            var topicType = "HelloWorld";
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

    @Test
    public void test_subscriber_notifies_publisher_when_it_closes() throws Exception {
        try (var publisherClient = new RtpsTalkClient()) {
            var topicName = "HelloWorldTopic";
            var topicType = "HelloWorld";
            var subscriberClient = new RtpsTalkClient();
            var collector =
                    new FixedCollectorSubscriber<>(new ArrayList<RtpsTalkDataMessage>(), 1) {
                        public void onNext(RtpsTalkDataMessage item) {
                            System.out.println(item);
                            super.onNext(item);
                        }
                    };
            var entityId = subscriberClient.subscribe(topicName, topicType, collector);
            var publisher = new SubmissionPublisher<RtpsTalkDataMessage>();
            publisherClient.publish(topicName, topicType, publisher);
            publisher.submit(new RtpsTalkDataMessage("hello"));
            collector.getFuture().get();
            subscriberClient.close();
            TestEvents.waitForDisposedSubscriber(
                    new Guid(subscriberClient.getConfiguration().guidPrefix(), entityId));
        }
    }

    /**
     * Once remote publisher sends N messages and local subscriber receives them they will both
     * initiate close.
     */
    @Test
    public void test_concurrent_close() throws Exception {
        try (var client = new RtpsTalkClient()) {
            var topicName = "HelloWorldTopic";
            var topicType = "HelloWorld";
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
     * Test that we replay changes in the history cache for BEST_EFFORT Subscribers. For RELIABLE
     * Subscribers it is not required as they suppose to request them through ACKNACKs
     */
    @Test
    public void test_best_effort_subscriber() throws Exception {
        var maxHistoryCacheSize = 200;
        try (var client =
                new RtpsTalkClient(
                        new RtpsTalkConfiguration.Builder()
                                .historyCacheMaxSize(maxHistoryCacheSize)
                                .build())) {
            var topicName = "HelloWorldTopic";
            var publisher = new SubmissionPublisher<RtpsTalkDataMessage>();
            client.publish(
                    topicName,
                    "HelloWorld",
                    new PublisherQosPolicy(
                            ReliabilityType.RELIABLE,
                            DurabilityType.TRANSIENT_LOCAL_DURABILITY_QOS),
                    publisher);
            tools.generateMessages(maxHistoryCacheSize).forEach(publisher::submit);
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
