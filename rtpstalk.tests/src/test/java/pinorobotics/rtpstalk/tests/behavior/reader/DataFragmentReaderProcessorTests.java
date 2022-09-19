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
        var fragment1 = "a".repeat(996);
        var fragment2 = "b".repeat(1000);
        var fragment3 = "c".repeat(800);
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer,
                        new DataFrag(
                                new EntityId(),
                                writer.entityId,
                                new SequenceNumber(100),
                                1,
                                10,
                                100,
                                2800,
                                Optional.empty(),
                                new SerializedPayload(new RawData(fragment1.getBytes()), true))));
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer,
                        new DataFrag(
                                new EntityId(),
                                writer.entityId,
                                new SequenceNumber(100),
                                11,
                                10,
                                100,
                                2800,
                                Optional.empty(),
                                new SerializedPayload(new RawData(fragment2.getBytes()), false))));

        var completeData = fragment1 + fragment2 + fragment3;
        Assertions.assertEquals(
                new RtpsTalkDataMessage(completeData).toString(),
                proc.addDataFrag(
                                writer,
                                new DataFrag(
                                        new EntityId(),
                                        writer.entityId,
                                        new SequenceNumber(100),
                                        21,
                                        8,
                                        100,
                                        2800,
                                        Optional.empty(),
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
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer1,
                        new DataFrag(
                                new EntityId(),
                                writer1.entityId,
                                new SequenceNumber(100),
                                1,
                                1,
                                2,
                                4,
                                Optional.empty(),
                                new SerializedPayload(new RawData("aa".getBytes()), false))));
        Assertions.assertEquals(
                Optional.empty(),
                proc.addDataFrag(
                        writer2,
                        new DataFrag(
                                new EntityId(),
                                writer2.entityId,
                                new SequenceNumber(100),
                                1,
                                1,
                                3,
                                6,
                                Optional.empty(),
                                new SerializedPayload(new RawData("eee".getBytes()), false))));
        Assertions.assertEquals(
                new RtpsTalkDataMessage("eeefff").toString(),
                proc.addDataFrag(
                                writer2,
                                new DataFrag(
                                        new EntityId(),
                                        writer2.entityId,
                                        new SequenceNumber(100),
                                        4,
                                        1,
                                        3,
                                        6,
                                        Optional.empty(),
                                        new SerializedPayload(
                                                new RawData("fff".getBytes()), false)))
                        .get()
                        .toString());
        Assertions.assertEquals(
                new RtpsTalkDataMessage("aabb").toString(),
                proc.addDataFrag(
                                writer1,
                                new DataFrag(
                                        new EntityId(),
                                        writer1.entityId,
                                        new SequenceNumber(100),
                                        3,
                                        1,
                                        2,
                                        4,
                                        Optional.empty(),
                                        new SerializedPayload(new RawData("bb".getBytes()), false)))
                        .get()
                        .toString());
    }
}
