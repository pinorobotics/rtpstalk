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
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.LogUtils;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsTalkClientTests extends PubSubClientTests {

    private FastRtpsExamples tools;

    static Stream<TestCase> dataProvider() {
        return Stream.of(new TestCase(RtpsTalkTestPubSubClient::new));
    }

    @BeforeEach
    public void setup() throws IOException {
        LogUtils.setupLog();
        tools = new FastRtpsExamples();
    }

    @AfterEach
    public void clean() {
        tools.close();
    }

    /**
     * Test that subscriber continues to receive messages when one publisher stopped and new one
     * joined.
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
                            System.out.println(item.data());
                            super.onNext(item);
                        }
                    };
            client.subscribe(topicName, topicType, collector);
            tools.runHelloWorldExample(Map.of(), "publisher").await();
            tools.runHelloWorldExample(Map.of(), "publisher").await();
            tools.runHelloWorldExample(Map.of(), "publisher").await();
            collector.getFuture().get();
        }
    }
}
