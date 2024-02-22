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
package pinorobotics.rtpstalk.tests.behavior.writer;

import id.xfunction.PreconditionException;
import id.xfunction.util.ImmutableMultiMap;
import id.xfunction.util.stream.XStream;
import id.xfunctiontests.XAsserts;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pinorobotics.rtpstalk.impl.behavior.writer.DataFragmentSplitter;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataFragmentSplitterTests {

    record TestCase(
            byte[] data,
            int maxSubmessageSize,
            Optional<ParameterList> inlineQos,
            String expectedFragmentsResource) {}

    public static Stream<TestCase> testData() {
        var minSubmessageSize = DataFrag.EMPTY_SUBMESSAGE_SIZE + SerializedPayloadHeader.SIZE;
        Assertions.assertEquals(36, minSubmessageSize);
        return Stream.of(
                new TestCase(
                        "abcdefg".getBytes(),
                        minSubmessageSize,
                        Optional.empty(),
                        "test_splitter_min_size"),
                new TestCase(
                        "abcd".getBytes(),
                        minSubmessageSize,
                        Optional.empty(),
                        "test_splitter_min_size_all_fragments_full"),
                new TestCase(
                        "abcd".getBytes(),
                        minSubmessageSize + 16,
                        Optional.empty(),
                        "test_splitter_single_fragment"),
                new TestCase(
                        "111122222222333".getBytes(),
                        minSubmessageSize + 4,
                        Optional.empty(),
                        "test_splitter_multi_fragment"),
                new TestCase(
                        "abcd".getBytes(),
                        minSubmessageSize + 16,
                        Optional.of(
                                ParameterList.ofUserParameters(
                                        ImmutableMultiMap.of((short) 0xbe, "1234".getBytes()))),
                        "test_splitter_inlineqos"));
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void test(TestCase testCase) {
        var writerSN = 17;
        var splitter =
                new DataFragmentSplitter(
                        TestConstants.TEST_TRACING_TOKEN,
                        new EntityId(1),
                        new EntityId(2),
                        writerSN,
                        testCase.inlineQos,
                        testCase.data,
                        testCase.maxSubmessageSize);
        var fragments = XStream.of(splitter.iterator()).toList();
        XAsserts.assertEquals(getClass(), testCase.expectedFragmentsResource, fragments.toString());
    }

    @Test
    public void test_unhappy() {
        var writerSN = 17;
        var e =
                Assertions.assertThrows(
                        PreconditionException.class,
                        () ->
                                new DataFragmentSplitter(
                                        TestConstants.TEST_TRACING_TOKEN,
                                        new EntityId(1),
                                        new EntityId(2),
                                        writerSN,
                                        Optional.empty(),
                                        "abcdefg".getBytes(),
                                        1));
        Assertions.assertEquals(
                "submessageSizeInBytes must be aligned on 32-bit boundary: 1", e.getMessage());
        e =
                Assertions.assertThrows(
                        PreconditionException.class,
                        () ->
                                new DataFragmentSplitter(
                                        TestConstants.TEST_TRACING_TOKEN,
                                        new EntityId(1),
                                        new EntityId(2),
                                        writerSN,
                                        Optional.empty(),
                                        "abcdefg".getBytes(),
                                        8));
        Assertions.assertEquals(
                "fragmentSize is too small: Value <4> should be less or equal <-24>",
                e.getMessage());
    }
}
