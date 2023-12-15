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
package pinorobotics.rtpstalk.tests.behavior.reader;

import id.xfunctiontests.XAsserts;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.behavior.reader.WriterHeartbeatProcessor;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.WriterProxy;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannelFactory;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class WriterHeartbeatProcessorTest {

    @Test
    public void test() {
        var dataChannelFactory = new TestDataChannelFactory();

        var wp =
                new WriterProxy(
                        TestConstants.TEST_TRACING_TOKEN,
                        dataChannelFactory,
                        TestConstants.TEST_CONFIG_INTERNAL.maxSubmessageSize(),
                        new Guid(
                                TestConstants.TEST_GUID_PREFIX,
                                EntityId.Predefined.ENTITYID_PARTICIPANT.getValue()),
                        new Guid(
                                TestConstants.TEST_REMOTE_GUID_PREFIX,
                                EntityId.Predefined.ENTITYID_PARTICIPANT.getValue()),
                        List.of(TestConstants.TEST_REMOTE_DEFAULT_UNICAST_LOCATOR));

        IntStream.range(1, 18).forEach(wp::receivedChangeSet);
        var proc =
                new WriterHeartbeatProcessor(
                        TestConstants.TEST_TRACING_TOKEN,
                        wp,
                        RtpsTalkConfiguration.MIN_PACKET_BUFFER_SIZE);
        proc.addHeartbeat(
                new Heartbeat(
                        TestConstants.TEST_READER_ENTITY_ID,
                        TestConstants.TEST_WRITER_ENTITY_ID,
                        10,
                        18,
                        123));

        proc.ack();
        var dataChannel =
                dataChannelFactory
                        .getChannels()
                        .get(TestConstants.TEST_REMOTE_DEFAULT_UNICAST_LOCATOR);
        XAsserts.assertMatches(getClass(), "test_heartbeat", dataChannel.getDataQueue().toString());
    }
}
