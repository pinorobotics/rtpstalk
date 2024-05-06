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

import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pinorobotics.rtpstalk.impl.behavior.reader.ReaderSequenceNumberStateBuilder;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ReaderSequenceNumberStateBuilderTest {

    record TestCase(long first, long last, long[] missing, String expected) {}

    static Stream<TestCase> dataProvider() {
        return Stream.of(
                new TestCase(
                        11,
                        20,
                        LongStream.range(0, 1003).toArray(),
                        """
                { "bitmapBase": { "value": 11 }, "numBits": "10", "bitmap": [1023] }
                """
                                .trim()),
                new TestCase(
                        11,
                        20,
                        LongStream.range(15, 18).toArray(),
                        """
                { "bitmapBase": { "value": 15 }, "numBits": "3", "bitmap": [7] }
                """
                                .trim()),
                new TestCase(
                        1,
                        SequenceNumberSet.BITMAP_SIZE,
                        LongStream.range(0, SequenceNumberSet.BITMAP_SIZE + 1).toArray(),
                        """
                { "bitmapBase": { "value": 1 }, "numBits": "256", "bitmap": [-1, -1, -1, -1, -1, -1, -1, -1] }
                """
                                .trim()),
                new TestCase(
                        11,
                        1234,
                        LongStream.range(0, 1003).toArray(),
                        """
                { "bitmapBase": { "value": 11 }, "numBits": "256", "bitmap": [-1, -1, -1, -1, -1, -1, -1, -1] }
                """
                                .trim()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void test(TestCase testCase) {
        var sets =
                new ReaderSequenceNumberStateBuilder()
                        .build(testCase.first, testCase.last, testCase.missing, 123);
        Assertions.assertEquals(testCase.expected, sets.toString());
    }
}
