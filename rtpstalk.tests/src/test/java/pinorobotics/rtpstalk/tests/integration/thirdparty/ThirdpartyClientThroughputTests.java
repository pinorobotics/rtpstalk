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
import id.pubsubtests.PubSubClientThroughputTestCase;
import id.pubsubtests.PubSubClientThroughputTests;
import id.pubsubtests.TestPubSubClient;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import pinorobotics.rtpstalk.tests.LogExtension;
import pinorobotics.rtpstalk.tests.integration.thirdparty.cyclonedds.CycloneDdsHelloWorldClient;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
@ExtendWith({ElasticsearchMetricsExtension.class, LogExtension.class})
public class ThirdpartyClientThroughputTests extends PubSubClientThroughputTests {

    static Stream<PubSubClientThroughputTestCase> dataProvider() {
        var messageFactory = new StringMessageFactory();
        Supplier<TestPubSubClient> clientFactory =
                () ->
                        new CompositePubSubClient(
                                new RtpsTalkHelloWorldClient(messageFactory),
                                new CycloneDdsHelloWorldClient());
        return Stream.of(
                /**
                 * Send 880 packages where each package size is 111 bytes and default. Expected time
                 * - less than 2sec
                 */
                new PubSubClientThroughputTestCase(
                        "test_publish_default",
                        clientFactory,
                        messageFactory,
                        111,
                        DEFAULT_DISCOVERY_PERIOD.plus(DEFAULT_HEARTBEAT_PERIOD).plusSeconds(2),
                        880,
                        Duration.ZERO,
                        true,
                        880));
    }
}
