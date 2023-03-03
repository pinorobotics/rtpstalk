/*
 * Copyright 2023 rtpstalk project
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
package pinorobotics.rtpstalk.tests;

import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.impl.PublisherDetails;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.qos.WriterQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.discovery.sedp.SedpBuiltinPublicationsWriter;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannelFactory;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class TestUtils {

    public static PublisherDetails newPublisherDetails() {
        var topicId = new TopicId("topic", "type");
        var qosPolicy = new WriterQosPolicySet();
        return new PublisherDetails(
                topicId, qosPolicy, new SubmissionPublisher<RtpsTalkDataMessage>());
    }

    public static SedpBuiltinPublicationsWriter newSedpPublicationsWriter(
            TestDataChannelFactory channelFactory) {
        return newSedpPublicationsWriter(
                channelFactory, new OperatingEntities(TestConstants.TEST_TRACING_TOKEN));
    }

    public static SedpBuiltinPublicationsWriter newSedpPublicationsWriter(
            TestDataChannelFactory channelFactory, OperatingEntities operatingEntities) {
        return new SedpBuiltinPublicationsWriter(
                TestConstants.TEST_CONFIG_INTERNAL,
                TestConstants.TEST_TRACING_TOKEN,
                TestConstants.TEST_PUBLISHER_EXECUTOR,
                channelFactory,
                operatingEntities);
    }
}