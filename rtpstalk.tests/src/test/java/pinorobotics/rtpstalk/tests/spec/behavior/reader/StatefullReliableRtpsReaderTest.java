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
package pinorobotics.rtpstalk.tests.spec.behavior.reader;

import id.xfunction.concurrent.SameThreadExecutorService;
import id.xfunction.concurrent.flow.CollectorSubscriber;
import id.xfunction.concurrent.flow.SynchronousPublisher;
import id.xfunctiontests.XAsserts;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Gap;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannelFactory;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class StatefullReliableRtpsReaderTest {

    @Test
    public void test_messages_from_non_matched_writers_are_ignored() {
        var reader =
                new StatefullReliableRtpsReader<>(
                        TestConstants.TEST_CONFIG_INTERNAL,
                        TestConstants.TEST_TRACING_TOKEN,
                        RtpsTalkDataMessage.class,
                        new SameThreadExecutorService(),
                        new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN),
                        TestConstants.TEST_READER_ENTITY_ID,
                        new ReaderQosPolicySet(
                                ReliabilityQosPolicy.Kind.RELIABLE,
                                DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS));
        var items = new ArrayList<RtpsTalkDataMessage>();
        reader.subscribe(new CollectorSubscriber<>(items));
        try (var publisher = new SynchronousPublisher<RtpsMessage>()) {
            publisher.subscribe(reader);

            var messages =
                    List.of(
                            RtpsReaderTest.newRtpsMessage(1, "aaaaa"),
                            RtpsReaderTest.newRtpsMessage(2, "bbbb"),
                            RtpsReaderTest.newRtpsMessage(5, "e"),
                            RtpsReaderTest.newRtpsMessage(4, "dd"),
                            RtpsReaderTest.newRtpsMessage(3, "ccc"),
                            RtpsReaderTest.newRtpsMessage(6, "ffff"));
            messages.forEach(publisher::submit);
            Assertions.assertEquals("[]", items.toString());

            reader.matchedWriterAdd(
                    TestConstants.TEST_GUID_WRITER,
                    List.of(TestConstants.TEST_DEFAULT_UNICAST_LOCATOR));
            messages.forEach(publisher::submit);
            XAsserts.assertEquals(getClass(), "test_RtpsReader_reliable2", items.toString());
        }
    }

    @Test
    public void test_transient_local_marks_previous_missing() {
        var dataChannelFactory = new TestDataChannelFactory();
        var reader =
                new StatefullReliableRtpsReader<>(
                        TestConstants.TEST_CONFIG_INTERNAL,
                        TestConstants.TEST_TRACING_TOKEN,
                        RtpsTalkDataMessage.class,
                        new SameThreadExecutorService(),
                        new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN),
                        TestConstants.TEST_READER_ENTITY_ID,
                        new ReaderQosPolicySet(
                                ReliabilityQosPolicy.Kind.RELIABLE,
                                DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS),
                        dataChannelFactory);
        var items = new ArrayList<RtpsTalkDataMessage>();
        reader.subscribe(new CollectorSubscriber<>(items));
        try (var publisher = new SynchronousPublisher<RtpsMessage>()) {
            publisher.subscribe(reader);
            reader.matchedWriterAdd(
                    TestConstants.TEST_GUID_WRITER,
                    List.of(TestConstants.TEST_DEFAULT_UNICAST_LOCATOR));
            reader.onHeartbeat(
                    TestConstants.TEST_GUID_WRITER.guidPrefix,
                    new Heartbeat(
                            TestConstants.TEST_READER_ENTITY_ID,
                            TestConstants.TEST_WRITER_ENTITY_ID,
                            1,
                            20,
                            11));
            publisher.submit(RtpsReaderTest.newRtpsMessage(3, "ccc"));
            Assertions.assertEquals("[]", items.toString());
            XAsserts.assertEquals(
                    getClass(),
                    "test_transient_local_marks_previous_missing",
                    dataChannelFactory
                            .getChannels()
                            .values()
                            .iterator()
                            .next()
                            .getDataQueue()
                            .toString());
        }
    }

    @Test
    public void test_gaps_are_skipped() {
        var dataChannelFactory = new TestDataChannelFactory();
        var reader =
                new StatefullReliableRtpsReader<>(
                        TestConstants.TEST_CONFIG_INTERNAL,
                        TestConstants.TEST_TRACING_TOKEN,
                        RtpsTalkDataMessage.class,
                        new SameThreadExecutorService(),
                        new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN),
                        TestConstants.TEST_READER_ENTITY_ID,
                        new ReaderQosPolicySet(
                                ReliabilityQosPolicy.Kind.RELIABLE,
                                DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS),
                        dataChannelFactory);
        var items = new ArrayList<RtpsTalkDataMessage>();
        reader.subscribe(new CollectorSubscriber<>(items));
        try (var publisher = new SynchronousPublisher<RtpsMessage>()) {
            publisher.subscribe(reader);
            reader.matchedWriterAdd(
                    TestConstants.TEST_GUID_WRITER,
                    List.of(TestConstants.TEST_DEFAULT_UNICAST_LOCATOR));
            reader.onGap(
                    TestConstants.TEST_GUID_WRITER.guidPrefix,
                    // gap between 1..2,3,4
                    new Gap(
                            TestConstants.TEST_READER_ENTITY_ID,
                            TestConstants.TEST_WRITER_ENTITY_ID,
                            new SequenceNumber(1),
                            new SequenceNumberSet(3, 1, new int[] {0b11})));
            publisher.submit(RtpsReaderTest.newRtpsMessage(6, "ccc"));
            Assertions.assertEquals("[]", items.toString());
            publisher.submit(RtpsReaderTest.newRtpsMessage(5, "bb"));
            Assertions.assertEquals(
                    """
                    [{ "inlineQos": "Optional.empty", "data": "62 62" }, { "inlineQos": "Optional.empty", "data": "63 63 63" }]""",
                    items.toString());
        }
    }
}
