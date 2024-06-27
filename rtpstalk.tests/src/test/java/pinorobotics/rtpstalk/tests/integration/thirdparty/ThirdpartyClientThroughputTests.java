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
package pinorobotics.rtpstalk.tests.integration.thirdparty;

import static pinorobotics.rtpstalk.RtpsTalkConfiguration.Builder.DEFAULT_DISCOVERY_PERIOD;
import static pinorobotics.rtpstalk.RtpsTalkConfiguration.Builder.DEFAULT_HEARTBEAT_PERIOD;

import id.opentelemetry.exporters.extensions.ElasticsearchMetricsExtension;
import id.pubsubtests.CompositePubSubClient;
import id.pubsubtests.MessageOrder;
import id.pubsubtests.PubSubClientThroughputTestCase;
import id.pubsubtests.PubSubClientThroughputTests;
import id.pubsubtests.TestPubSubClient;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.qos.DurabilityType;
import pinorobotics.rtpstalk.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.qos.ReliabilityType;
import pinorobotics.rtpstalk.qos.SubscriberQosPolicy;
import pinorobotics.rtpstalk.tests.LogExtension;
import pinorobotics.rtpstalk.tests.integration.thirdparty.cyclonedds.CycloneDdsHelloWorldClient;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
// These tests relies on CycloneDdsHelloWorldClient which currently is not available for Windows
@DisabledOnOs({OS.WINDOWS})
@ExtendWith({ElasticsearchMetricsExtension.class, LogExtension.class})
public class ThirdpartyClientThroughputTests extends PubSubClientThroughputTests {

    private static Supplier<TestPubSubClient> clientFactory(
            RtpsTalkConfiguration config,
            PublisherQosPolicy publisherQos,
            SubscriberQosPolicy subscriberQos,
            StringMessageFactory messageFactory) {
        return () ->
                new CompositePubSubClient(
                        new RtpsTalkHelloWorldClient(config, subscriberQos, messageFactory),
                        new CycloneDdsHelloWorldClient(
                                Map.of(
                                        HelloWorldExampleVariable.ReliabilityQosPolicyKind,
                                        publisherQos.reliabilityType() == ReliabilityType.RELIABLE
                                                ? "RELIABLE_RELIABILITY"
                                                : "BEST_EFFORT_RELIABILITY")));
    }

    static Stream<PubSubClientThroughputTestCase> dataProvider() {
        var messageFactory = new StringMessageFactory();
        return Stream.of(
                /**
                 * Send 880 packages where each package size is 111 bytes and default. Expected time
                 * - less than 2sec
                 */
                new PubSubClientThroughputTestCase(
                        "test_publish_default",
                        clientFactory(
                                new RtpsTalkConfiguration.Builder().build(),
                                new PublisherQosPolicy(),
                                new SubscriberQosPolicy(),
                                messageFactory),
                        messageFactory,
                        111,
                        DEFAULT_DISCOVERY_PERIOD.plus(DEFAULT_HEARTBEAT_PERIOD).plusSeconds(2),
                        880,
                        Duration.ZERO,
                        true,
                        MessageOrder.STRICT_ASCENDING,
                        880),
                /** Test BEST_EFFORT subscriber */
                new PubSubClientThroughputTestCase(
                        "test_publish_best_effort_subscriber",
                        clientFactory(
                                new RtpsTalkConfiguration.Builder().build(),
                                new PublisherQosPolicy(
                                        ReliabilityType.BEST_EFFORT,
                                        DurabilityType.VOLATILE_DURABILITY_QOS),
                                new SubscriberQosPolicy(
                                        ReliabilityType.BEST_EFFORT,
                                        DurabilityType.VOLATILE_DURABILITY_QOS),
                                messageFactory),
                        messageFactory,
                        111,
                        DEFAULT_DISCOVERY_PERIOD.plus(DEFAULT_HEARTBEAT_PERIOD).plusSeconds(2),
                        880,
                        Duration.ZERO,
                        true,
                        MessageOrder.ASCENDING,
                        880));
    }
}
