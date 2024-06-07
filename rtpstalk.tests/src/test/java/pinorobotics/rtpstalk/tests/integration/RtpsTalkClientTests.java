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

import static pinorobotics.rtpstalk.RtpsTalkConfiguration.Builder.DEFAULT_DISCOVERY_PERIOD;

import id.opentelemetry.exporters.extensions.ElasticsearchMetricsExtension;
import id.pubsubtests.PubSubClientTestCase;
import id.pubsubtests.PubSubClientTests;
import id.xfunction.concurrent.flow.FixedCollectorSubscriber;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.qos.DurabilityType;
import pinorobotics.rtpstalk.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.qos.ReliabilityType;
import pinorobotics.rtpstalk.tests.LogExtension;
import pinorobotics.rtpstalk.tests.TestEvents;

/**
 * @author lambdaprime intid@protonmail.com
 */
@ExtendWith({ElasticsearchMetricsExtension.class, LogExtension.class})
public class RtpsTalkClientTests extends PubSubClientTests {

    static Stream<PubSubClientTestCase> dataProvider() {
        return Stream.of(
                new PubSubClientTestCase(
                        "test_with_default_settings",
                        RtpsTalkTestPubSubClient::new,
                        DEFAULT_DISCOVERY_PERIOD,
                        RtpsTalkTestPubSubClient.QUEUE_SIZE));
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
}
