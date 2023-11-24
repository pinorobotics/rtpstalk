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

import id.pubsubtests.PubSubClientThroughputTestCase;
import id.pubsubtests.PubSubClientThroughputTests;
import id.pubsubtests.TestPubSubClient;
import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import pinorobotics.rtpstalk.tests.LogUtils;
import pinorobotics.rtpstalk.tests.MetricsExtension;

/**
 * @author lambdaprime intid@protonmail.com
 */
@ExtendWith({MetricsExtension.class})
public class RtpsTalkClientThroughputTests extends PubSubClientThroughputTests {

    static Stream<PubSubClientThroughputTestCase> dataProvider() {
        Supplier<TestPubSubClient> clientFactory = RtpsTalkTestPubSubClient::new;
        return Stream.of(
                new PubSubClientThroughputTestCase(
                        "test_publish_multiple_60kb_messages",
                        clientFactory,
                        Duration.ofSeconds(40),
                        60_000,
                        83, // 60kb packages with total data 5mb
                        Duration.ZERO,
                        83),
                new PubSubClientThroughputTestCase(
                        "test_publish_single_message_over_5mb",
                        clientFactory,
                        Duration.ofMillis(13_000),
                        5_123_456,
                        1,
                        Duration.ZERO,
                        1),

                // Constantly publish messages over 5mb for period of 1 minute. Expect  Subscriber
                // to receive at least 80 messages
                new PubSubClientThroughputTestCase(
                        "test_throutput",
                        clientFactory,
                        Duration.ofMinutes(1),
                        5_123_456,
                        Integer.MAX_VALUE,
                        Duration.ofMillis(300),
                        80));
    }

    @BeforeEach
    public void setup() throws IOException {
        LogUtils.setupLog();
    }

    @AfterEach
    public void clean(TestInfo testInfo) {
        LogUtils.archiveLog(testInfo);
    }
}
