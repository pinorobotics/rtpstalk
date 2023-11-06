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
package pinorobotics.rtpstalk.tests.behavior.reader;

import id.xfunction.PreconditionException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.behavior.reader.DataFragmentReaderProcessor;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataFragmentReaderProcessorTests {

    @Test
    public void test_fragmentation() {
        var proc = new DataFragmentReaderProcessor(TestConstants.TEST_TRACING_TOKEN);
        var writer = new Guid(TestConstants.TEST_GUID_PREFIX, new EntityId(1));
        var dataFragGenerator =
                new DataFragGenerator(
                        new EntityId(),
                        writer.entityId,
                        new SequenceNumber(100),
                        100,
                        2800,
                        Optional.empty());
        var fragment1 = "a".repeat(996);
        var fragment2 = "b".repeat(1000);
        var fragment3 = "c".repeat(800);
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer,
                        dataFragGenerator.generate(
                                1,
                                10,
                                new SerializedPayload(new RawData(fragment1.getBytes()), true))));
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer,
                        dataFragGenerator.generate(
                                11,
                                10,
                                new SerializedPayload(new RawData(fragment2.getBytes()), false))));

        var completeData = fragment1 + fragment2 + fragment3;
        Assertions.assertEquals(
                new RtpsTalkDataMessage(completeData).toString(),
                proc.addDataFrag(
                                writer,
                                dataFragGenerator.generate(
                                        21,
                                        8,
                                        new SerializedPayload(
                                                new RawData(fragment3.getBytes()), false)))
                        .get()
                        .toString());
    }

    @Test
    public void test_many_writers() {
        var proc = new DataFragmentReaderProcessor(TestConstants.TEST_TRACING_TOKEN);
        var writer1 = new Guid(TestConstants.TEST_GUID_PREFIX, new EntityId(1));
        var writer2 = new Guid(TestConstants.TEST_GUID_PREFIX, new EntityId(2));
        var dataFragGenerator1 =
                new DataFragGenerator(
                        new EntityId(),
                        writer1.entityId,
                        new SequenceNumber(100),
                        5,
                        10,
                        Optional.empty());
        var dataFragGenerator2 =
                new DataFragGenerator(
                        new EntityId(),
                        writer2.entityId,
                        new SequenceNumber(100),
                        7,
                        14,
                        Optional.empty());
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer1,
                        dataFragGenerator1.generate(
                                1, 1, new SerializedPayload(new RawData("a".getBytes()), true))));
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer2,
                        dataFragGenerator2.generate(
                                1, 1, new SerializedPayload(new RawData("eee".getBytes()), true))));
        Assertions.assertEquals(
                new RtpsTalkDataMessage("eeefffffff").toString(),
                proc.addDataFrag(
                                writer2,
                                dataFragGenerator2.generate(
                                        2,
                                        1,
                                        new SerializedPayload(
                                                new RawData("fffffff".getBytes()), false)))
                        .get()
                        .toString());
        Assertions.assertEquals(
                new RtpsTalkDataMessage("aaabbb").toString(),
                proc.addDataFrag(
                                writer1,
                                dataFragGenerator1.generate(
                                        2,
                                        1,
                                        new SerializedPayload(
                                                new RawData("aabbb".getBytes()), false)))
                        .get()
                        .toString());
    }

    @Test
    public void test_no_padding_no_fragment_underflow() {
        var proc = new DataFragmentReaderProcessor(TestConstants.TEST_TRACING_TOKEN);
        var writer = new Guid(TestConstants.TEST_GUID_PREFIX, new EntityId(1));
        var dataFragGenerator =
                new DataFragGenerator(
                        new EntityId(),
                        writer.entityId,
                        new SequenceNumber(16),
                        8,
                        16,
                        Optional.empty());
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer,
                        dataFragGenerator.generate(
                                1,
                                1,
                                new SerializedPayload(new RawData("aaaa".getBytes()), true))));
        Assertions.assertEquals(
                new RtpsTalkDataMessage("aaaabbbbbbbb").toString(),
                proc.addDataFrag(
                                writer,
                                dataFragGenerator.generate(
                                        2,
                                        1,
                                        new SerializedPayload(
                                                new RawData("bbbbbbbb".getBytes()), false)))
                        .get()
                        .toString());
    }

    @Test
    public void test_inorder_order_with_non_last_fragment_underflow() {
        var proc = new DataFragmentReaderProcessor(TestConstants.TEST_TRACING_TOKEN);
        var writer = new Guid(TestConstants.TEST_GUID_PREFIX, new EntityId(1));
        var dataFragGenerator =
                new DataFragGenerator(
                        new EntityId(),
                        writer.entityId,
                        new SequenceNumber(160),
                        7,
                        16,
                        Optional.empty());
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer,
                        dataFragGenerator.generate(
                                1, 1, new SerializedPayload(new RawData("aaa".getBytes()), true))));
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer,
                        dataFragGenerator.generate(
                                2,
                                1,
                                new SerializedPayload(new RawData("bbbbbff".getBytes()), false))));
        Assertions.assertEquals(
                // 4 bytes for header
                new RtpsTalkDataMessage("aaabbbbbffcc").toString(),
                proc.addDataFrag(
                                writer,
                                dataFragGenerator.generate(
                                        3,
                                        1,
                                        new SerializedPayload(new RawData("cc".getBytes()), false)))
                        .get()
                        .toString());
    }

    /**
     * Expected fragments size is 10 but last fragment has only 6 bytes of data. Because we expect 6
     * bytes from last fragment we join the message and return it.
     */
    @Test
    public void test_random_order_with_last_fragment_underflow() {
        var proc = new DataFragmentReaderProcessor(TestConstants.TEST_TRACING_TOKEN);
        var writer = new Guid(TestConstants.TEST_GUID_PREFIX, new EntityId(1));
        var dataFragGenerator =
                new DataFragGenerator(
                        new EntityId(),
                        writer.entityId,
                        new SequenceNumber(16),
                        10,
                        16,
                        Optional.empty());
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer,
                        dataFragGenerator.generate(
                                2,
                                1,
                                new SerializedPayload(new RawData("cbbbbf".getBytes()), false))));
        Assertions.assertEquals(
                // 4 bytes for header
                new RtpsTalkDataMessage("aaaabbcbbbbf").toString(),
                proc.addDataFrag(
                                writer,
                                dataFragGenerator.generate(
                                        1,
                                        1,
                                        new SerializedPayload(
                                                new RawData("aaaabb".getBytes()), true)))
                        .get()
                        .toString());
    }

    /**
     * Expected fragments size is 10 but last fragment has only 4 bytes of data. Because we expect 5
     * bytes from last fragment but it has 1 byte missing - throw exception.
     */
    @Test
    public void test_random_order_with_last_fragment_data_missing() {
        var proc = new DataFragmentReaderProcessor(TestConstants.TEST_TRACING_TOKEN);
        var writer = new Guid(TestConstants.TEST_GUID_PREFIX, new EntityId(1));
        var e =
                Assertions.assertThrows(
                        PreconditionException.class,
                        () ->
                                proc.addDataFrag(
                                        writer,
                                        new DataFrag(
                                                new EntityId(),
                                                writer.entityId,
                                                new SequenceNumber(16),
                                                3,
                                                1,
                                                10,
                                                25,
                                                Optional.empty(),
                                                new SerializedPayload(
                                                        new RawData("bfbb".getBytes()), false))));
        Assertions.assertEquals(
                "TEST-dataSequenceNumber16: Last fragment length delta underflow: -1",
                e.getMessage());
    }
}
