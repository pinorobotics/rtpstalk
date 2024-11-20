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
package pinorobotics.rtpstalk.tests.integration;

import static pinorobotics.rtpstalk.RtpsTalkConfiguration.Builder.DEFAULT_DISCOVERY_PERIOD;
import static pinorobotics.rtpstalk.RtpsTalkConfiguration.Builder.DEFAULT_HEARTBEAT_PERIOD;

import id.opentelemetry.exporters.extensions.ElasticsearchMetricsExtension;
import id.pubsubtests.PubSubClientLatencyTestCase;
import id.pubsubtests.PubSubClientLatencyTests;
import java.util.stream.Stream;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.WriterSettings;
import pinorobotics.rtpstalk.tests.LogExtension;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
// Windows tests are run inside VM so they show different results here and are excluded
@DisabledOnOs({OS.WINDOWS})
@ExtendWith({ElasticsearchMetricsExtension.class, LogExtension.class})
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
                        "test_latency_small_queue_pushMode_disabled",
                        () ->
                                new RtpsTalkTestPubSubClient(
                                        new RtpsTalkConfiguration.Builder()
                                                .domainId(123)
                                                .historyCacheMaxSize(17)
                                                .publisherMaxBufferSize(1)
                                                .build(),
                                        new WriterSettings(false)),
                        // avoid measuring latency which is caused due to discovery protocol
                        DEFAULT_DISCOVERY_PERIOD.plus(DEFAULT_HEARTBEAT_PERIOD).plusSeconds(1),
                        // how long to run test
                        DEFAULT_DISCOVERY_PERIOD.plusSeconds(20),
                        60_000,
                        DEFAULT_HEARTBEAT_PERIOD.multipliedBy(4).plusMillis(60),
                        120),
                /**
                 * With push mode enabled latency for message M reduces to one heartbeat cycle (to
                 * cleanup the history cache).
                 *
                 * <p>It also increases throughput from ~120 messages to ~250
                 */
                new PubSubClientLatencyTestCase(
                        "test_latency_small_queue_pushMode_enabled",
                        () ->
                                new RtpsTalkTestPubSubClient(
                                        new RtpsTalkConfiguration.Builder()
                                                .historyCacheMaxSize(15)
                                                .publisherMaxBufferSize(1)
                                                .build(),
                                        new WriterSettings(true)),
                        // avoid measuring latency which is caused due to discovery protocol
                        DEFAULT_DISCOVERY_PERIOD.plus(DEFAULT_HEARTBEAT_PERIOD).plusSeconds(1),
                        // how long to run test
                        DEFAULT_DISCOVERY_PERIOD.plusSeconds(20),
                        60_000,
                        DEFAULT_HEARTBEAT_PERIOD.plusMillis(60),
                        250));
    }
}
