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

import id.pubsubtests.PubSubClientLatencyTestCase;
import id.pubsubtests.PubSubClientLatencyTests;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.tests.LogExtension;
import pinorobotics.rtpstalk.tests.MetricsExtension;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
@ExtendWith({MetricsExtension.class, LogExtension.class})
public class RtpsTalkClientLatencyTests extends PubSubClientLatencyTests {

    static Stream<PubSubClientLatencyTestCase> dataProvider() {
        return Stream.of(
                /**
                 * For {@link HistoryQosPolicy#Kind#KEEP_ALL_HISTORY_QOS} once Writer history queue
                 * becomes full new message M will not be added until next two heartbeat cycles take
                 * place (each equal to {@link RtpsTalkConfiguration#heartbeatPeriod()}).
                 *
                 * <ul>
                 *   <li>first heartbeat cycle for sending heartbeat
                 *   <li>second heartbeat cycle for sending requested changes
                 * </ul>
                 *
                 * <p>This means that message M total latency expected to be at least: 4 * {@link
                 * RtpsTalkConfiguration#heartbeatPeriod()} (2 HB cycles for waiting queue to be
                 * freed, 2 HB cycles to be sent to the Subscriber)
                 */
                new PubSubClientLatencyTestCase(
                        "test_latency_small_queue",
                        () ->
                                new RtpsTalkTestPubSubClient(
                                        new RtpsTalkConfiguration.Builder()
                                                .historyCacheMaxSize(17)
                                                .publisherMaxBufferSize(1)
                                                .build()),
                        // avoid measuring latency which is caused due to discovery protocol
                        RtpsTalkConfiguration.Builder.DEFAULT_DISCOVERY_PERIOD.plusSeconds(1),
                        // how long to run test
                        RtpsTalkConfiguration.Builder.DEFAULT_DISCOVERY_PERIOD.plusSeconds(20),
                        60_000,
                        Duration.ofMillis(4060)));
    }
}
