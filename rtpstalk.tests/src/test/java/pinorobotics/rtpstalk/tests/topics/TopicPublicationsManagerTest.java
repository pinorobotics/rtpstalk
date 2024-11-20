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
package pinorobotics.rtpstalk.tests.topics;

import id.xfunction.PreconditionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.topics.LocalTopicPublicationsManager;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.TestRtpsNetworkInterface;
import pinorobotics.rtpstalk.tests.TestUtils;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannelFactory;
import pinorobotics.rtpstalk.tests.spec.userdata.TestUserDataService;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class TopicPublicationsManagerTest {

    @Test
    public void test_addLocalActor() throws Exception {
        var manager =
                new LocalTopicPublicationsManager(
                        TestConstants.TEST_TRACING_TOKEN,
                        TestConstants.TEST_CONFIG_INTERNAL,
                        new TestRtpsNetworkInterface(),
                        TestUtils.newSedpPublicationsWriter(new TestDataChannelFactory()),
                        new TestUserDataService());
        manager.addLocalActor(TestUtils.newPublisherDetails());
        var exception =
                Assertions.assertThrows(
                        PreconditionException.class,
                        () -> manager.addLocalActor(TestUtils.newPublisherDetails()));
        Assertions.assertEquals(
                """
                Only one local writer per topic { "name": "topic", "type": "type" } is allowed
                """
                        .trim(),
                exception.getMessage());
    }
}
