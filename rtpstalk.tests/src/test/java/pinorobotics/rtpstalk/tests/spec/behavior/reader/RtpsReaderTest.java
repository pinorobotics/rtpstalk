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
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.RtpsReader;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsReaderTest {

    record TestCase(
            String name,
            RtpsReader<RtpsTalkDataMessage> reader,
            List<RtpsMessage> messages,
            String expectedResourceName) {
        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<TestCase> dataProvider() {
        return Stream.of(
                // test that we ignore messages which targeted to different readers
                // test that Reader process message it receives and submits to its subscribers
                // test that Reader does not submit duplicates
                // test that Reader process message it receives and submits to its subscribers
                // test that BEST_EFFORT Reader submits messages only in increasing order
                new TestCase(
                        "BEST_EFFORT",
                        new RtpsReader<>(
                                TestConstants.TEST_CONFIG,
                                TestConstants.TEST_TRACING_TOKEN,
                                RtpsTalkDataMessage.class,
                                new SameThreadExecutorService(),
                                TestConstants.TEST_GUID_READER,
                                ReliabilityQosPolicy.Kind.BEST_EFFORT),
                        List.of(
                                // different reader message is ignored
                                new RtpsMessage(
                                        TestConstants.TEST_HEADER,
                                        new Data(
                                                new EntityId(0x12, EntityKind.READER_NO_KEY),
                                                new EntityId(0x01, EntityKind.WRITER_NO_KEY),
                                                new SequenceNumber(1),
                                                new SerializedPayload(
                                                        new RawData("hello".getBytes()), true))),
                                newRtpsMessage(2, "aaaa"),
                                newRtpsMessage(2, "aaaaa"),
                                newRtpsMessage(3, "bbbb"),
                                newRtpsMessage(6, "d"),
                                newRtpsMessage(5, "cc")),
                        "test_RtpsReader"),

                // test that RELIABLE Reader never submits messages out of order
                new TestCase(
                        "RELIABLE1",
                        new StatefullReliableRtpsReader<>(
                                TestConstants.TEST_CONFIG_INTERNAL,
                                TestConstants.TEST_TRACING_TOKEN,
                                RtpsTalkDataMessage.class,
                                new SameThreadExecutorService(),
                                new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN),
                                TestConstants.TEST_READER_ENTITY_ID,
                                new ReaderQosPolicySet(
                                        ReliabilityQosPolicy.Kind.RELIABLE,
                                        DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS)),
                        List.of(
                                // different reader message is ignored
                                new RtpsMessage(
                                        TestConstants.TEST_HEADER,
                                        new Data(
                                                new EntityId(0x12, EntityKind.READER_NO_KEY),
                                                new EntityId(0x01, EntityKind.WRITER_NO_KEY),
                                                new SequenceNumber(1),
                                                new SerializedPayload(
                                                        new RawData("hello".getBytes()), true))),
                                newRtpsMessage(1, "aaaa"),
                                newRtpsMessage(1, "aaaaa"),
                                newRtpsMessage(2, "bbbb"),
                                newRtpsMessage(5, "d"),
                                newRtpsMessage(4, "cc")),
                        "test_RtpsReader_reliable1"),

                // test that RELIABLE Reader submits messages in strictly sequential order
                new TestCase(
                        "RELIABLE2",
                        new StatefullReliableRtpsReader<>(
                                TestConstants.TEST_CONFIG_INTERNAL,
                                TestConstants.TEST_TRACING_TOKEN,
                                RtpsTalkDataMessage.class,
                                new SameThreadExecutorService(),
                                new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN),
                                TestConstants.TEST_READER_ENTITY_ID,
                                new ReaderQosPolicySet(
                                        ReliabilityQosPolicy.Kind.RELIABLE,
                                        DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS)),
                        List.of(
                                newRtpsMessage(1, "aaaaa"),
                                newRtpsMessage(2, "bbbb"),
                                newRtpsMessage(5, "e"),
                                newRtpsMessage(4, "dd"),
                                newRtpsMessage(3, "ccc"),
                                newRtpsMessage(6, "ffff")),
                        "test_RtpsReader_reliable2"),

                // test that TRANSIENT_LOCAL_DURABILITY_QOS Reader submits messages only starting
                // from message sequence number 1
                new TestCase(
                        "RELIABLE2",
                        new StatefullReliableRtpsReader<>(
                                TestConstants.TEST_CONFIG_INTERNAL,
                                TestConstants.TEST_TRACING_TOKEN,
                                RtpsTalkDataMessage.class,
                                new SameThreadExecutorService(),
                                new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN),
                                TestConstants.TEST_READER_ENTITY_ID,
                                new ReaderQosPolicySet(
                                        ReliabilityQosPolicy.Kind.RELIABLE,
                                        DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS)),
                        List.of(newRtpsMessage(15, "aaaaa"), newRtpsMessage(16, "bbbb")),
                        "test_empty_list"),

                // test that VOLATILE_DURABILITY_QOS Reader submits messages starting from first
                // received
                new TestCase(
                        "RELIABLE2",
                        new StatefullReliableRtpsReader<>(
                                TestConstants.TEST_CONFIG_INTERNAL,
                                TestConstants.TEST_TRACING_TOKEN,
                                RtpsTalkDataMessage.class,
                                new SameThreadExecutorService(),
                                new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN),
                                TestConstants.TEST_READER_ENTITY_ID,
                                new ReaderQosPolicySet(
                                        ReliabilityQosPolicy.Kind.RELIABLE,
                                        DurabilityQosPolicy.Kind.VOLATILE_DURABILITY_QOS)),
                        List.of(
                                newRtpsMessage(15, "aaaaa"),
                                newRtpsMessage(18, "d"),
                                newRtpsMessage(16, "bbbb"),
                                newRtpsMessage(17, "ccc")),
                        "test_RtpsReader_VOLATILE_DURABILITY_QOS"));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void test(TestCase testCase) {
        var items = new ArrayList<RtpsTalkDataMessage>();
        var reader = testCase.reader;
        if (reader instanceof StatefullReliableRtpsReader<RtpsTalkDataMessage> statefulReader) {
            statefulReader.matchedWriterAdd(
                    TestConstants.TEST_GUID_WRITER,
                    List.of(TestConstants.TEST_DEFAULT_UNICAST_LOCATOR));
        }
        reader.subscribe(new CollectorSubscriber<>(items));
        try (var publisher = new SynchronousPublisher<RtpsMessage>()) {
            publisher.subscribe(reader);

            testCase.messages.forEach(publisher::submit);

            XAsserts.assertEquals(getClass(), testCase.expectedResourceName, items.toString());
        }
    }

    public static RtpsMessage newRtpsMessage(int seqNum, String data) {
        return new RtpsMessage(
                TestConstants.TEST_HEADER,
                new Data(
                        TestConstants.TEST_READER_ENTITY_ID,
                        TestConstants.TEST_WRITER_ENTITY_ID,
                        new SequenceNumber(seqNum),
                        new SerializedPayload(new RawData(data.getBytes()), true)));
    }
}
