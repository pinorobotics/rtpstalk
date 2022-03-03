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
package pinorobotics.rtpstalk.tests.discovery.spdp;

import id.xfunction.ResourceUtils;
import id.xfunction.text.WildcardMatcher;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.discovery.spdp.SpdpService;
import pinorobotics.rtpstalk.tests.TestConstants;

/** @author lambdaprime intid@protonmail.com */
public class SpdpServiceTest {
    private static final ResourceUtils resourceUtils = new ResourceUtils();
    private static final RtpsTalkConfiguration CONFIG = TestConstants.TEST_CONFIG;
    private static final TestDataChannelFactory channelFactory = new TestDataChannelFactory(CONFIG);

    private SpdpService service;

    @BeforeEach
    public void setup() {
        service = new SpdpService(CONFIG, channelFactory);
    }

    @AfterEach
    public void cleanup() throws Exception {
        service.close();
    }

    @Test
    public void test_happy() throws Exception {
        service.start();
        // we expect spdp startup time no longer than 100 msec
        Thread.sleep(100);
        var channel = channelFactory.getChannels().get(CONFIG.getMetatrafficMulticastLocator());
        Assertions.assertNotNull(channel);
        Assertions.assertEquals(1, channel.getOutput().size());
        var message = channel.getOutput().get(0).toString();
        System.out.println(message);
        Assertions.assertTrue(
                new WildcardMatcher(
                                resourceUtils.readResource(
                                        getClass(), "SpdpDiscoveredParticipantData"))
                        .matches(message));
    }

    @Test
    public void test_rate() throws Exception {
        new SpdpService(
                        new RtpsTalkConfiguration()
                                .withSpdpDiscoveredParticipantDataPublishPeriod(
                                        Duration.ofMillis(50)),
                        channelFactory)
                .start();
        Thread.sleep(160);
        var channel = channelFactory.getChannels().get(CONFIG.getMetatrafficMulticastLocator());
        Assertions.assertNotNull(channel);
        Assertions.assertEquals(4, channel.getOutput().size());
    }
}
