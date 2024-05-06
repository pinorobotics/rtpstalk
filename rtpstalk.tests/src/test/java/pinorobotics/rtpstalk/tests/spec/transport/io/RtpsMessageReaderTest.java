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
package pinorobotics.rtpstalk.tests.spec.transport.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import id.xfunction.PreconditionException;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageReader;
import pinorobotics.rtpstalk.tests.spec.transport.io.DataProviders.TestCase;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsMessageReaderTest {

    @ParameterizedTest
    @MethodSource(
            "pinorobotics.rtpstalk.tests.spec.transport.io.DataProviders#rtpsMessageConversion")
    public void testRead(TestCase testData) throws Exception {
        var buf = ByteBuffer.wrap(testData.serializedMessage());
        var expected = testData.message();
        var actual = new RtpsMessageReader().readRtpsMessage(buf).get();
        System.out.println(actual);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void testRead_validate() throws Exception {
        var buf = ByteBuffer.wrap(DataProviders.readAllBytes("test_data_invalid_zero_writerSN"));
        Assertions.assertThrows(
                PreconditionException.class, () -> new RtpsMessageReader().readRtpsMessage(buf));
    }
}
