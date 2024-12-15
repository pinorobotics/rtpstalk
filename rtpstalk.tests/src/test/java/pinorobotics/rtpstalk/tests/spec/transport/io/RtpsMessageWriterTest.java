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
package pinorobotics.rtpstalk.tests.spec.transport.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import id.xfunction.XByte;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageWriter;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.spec.transport.io.DataProviders.TestCase;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsMessageWriterTest {

    @ParameterizedTest
    @MethodSource(
            "pinorobotics.rtpstalk.tests.spec.transport.io.DataProviders#rtpsMessageConversion")
    public void testWrite(TestCase testData) throws Exception {
        var buf = ByteBuffer.allocate(TestConstants.TEST_CONFIG.packetBufferSize());
        new RtpsMessageWriter().writeRtpsMessage(testData.message(), buf);
        buf.limit(buf.position());
        buf.rewind();
        var actual = new byte[buf.limit()];
        buf.get(actual);
        System.out.println(XByte.toHexPairs(actual));
        assertEquals(XByte.toHexPairs(testData.serializedMessage()), XByte.toHexPairs(actual));
    }

    @Test
    public void test_write_sequenceNumber() throws Exception {
        var buf = ByteBuffer.allocate(SequenceNumber.SIZE);
        new RtpsMessageWriter().write(SequenceNumber.SEQUENCENUMBER_UNKNOWN, buf);
        assertArrayEquals(XByte.copyToByteArray(-1, 0), buf.array());
    }
}
