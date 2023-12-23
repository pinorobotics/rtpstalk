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
package pinorobotics.rtpstalk.tests.spec.behavior.writer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.WriterSettings;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.qos.WriterQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.WriterRtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.AckNack;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannelFactory;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class WriterRtpsReaderTest {

    @Test
    public void test_cleanupCache() throws IOException {
        // will be updated from background thread
        var counter = new AtomicInteger();
        var writerMock =
                new StatefullReliableRtpsWriter<RtpsTalkDataMessage>(
                        TestConstants.TEST_CONFIG_INTERNAL,
                        TestConstants.TEST_TRACING_TOKEN,
                        TestConstants.TEST_PUBLISHER_EXECUTOR,
                        new TestDataChannelFactory(TestConstants.TEST_CONFIG),
                        new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN),
                        TestConstants.TEST_WRITER_ENTITY_ID,
                        new WriterQosPolicySet(
                                ReliabilityQosPolicy.Kind.RELIABLE,
                                DurabilityQosPolicy.Kind.VOLATILE_DURABILITY_QOS),
                        new WriterSettings()) {
                    @Override
                    protected void cleanupCache() {
                        counter.incrementAndGet();
                    }
                };
        // 1. Adding first reader starts new background thread to cleanup the cache periodically
        writerMock.matchedReaderAdd(
                TestConstants.TEST_GUID_READER,
                List.of(TestConstants.TEST_DEFAULT_UNICAST_LOCATOR),
                new ReaderQosPolicySet());
        // wait for background thread
        while (counter.get() == 0)
            ;

        var reader =
                new WriterRtpsReader<RtpsTalkDataMessage>(
                        TestConstants.TEST_TRACING_TOKEN, writerMock);
        // 2. Reader acked everything up to 253 so we expect cache cleanup as well
        reader.onAckNack(
                TestConstants.TEST_GUID_PREFIX,
                new AckNack(
                        TestConstants.TEST_READER_ENTITY_ID,
                        TestConstants.TEST_WRITER_ENTITY_ID,
                        // expecting 253
                        new SequenceNumberSet(253, 0),
                        12));
        Assertions.assertEquals(2, counter.get());
    }
}
