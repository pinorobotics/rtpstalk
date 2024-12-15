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

import static org.junit.jupiter.api.Assertions.assertEquals;

import id.xfunction.PreconditionException;
import id.xfunction.ResourceUtils;
import id.xfunction.XByte;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HexFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.messages.SampleIdentity;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageReader;
import pinorobotics.rtpstalk.tests.spec.transport.io.DataProviders.TestCase;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsMessageReaderTest {
    private static final ResourceUtils resourceUtils = new ResourceUtils();

    @ParameterizedTest
    @MethodSource(
            "pinorobotics.rtpstalk.tests.spec.transport.io.DataProviders#rtpsMessageConversion")
    public void test(TestCase testData) throws Exception {
        var buf = ByteBuffer.wrap(testData.serializedMessage());
        var expected = testData.message();
        var actual = new RtpsMessageReader().readRtpsMessage(buf).get();
        System.out.println(actual);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void test_validate() throws Exception {
        var buf = ByteBuffer.wrap(DataProviders.readAllBytes("test_data_invalid_zero_writerSN"));
        Assertions.assertThrows(
                PreconditionException.class, () -> new RtpsMessageReader().readRtpsMessage(buf));
    }

    @Test
    public void test_skip_unknown_submessages() throws Exception {
        var buf = ByteBuffer.wrap(DataProviders.readAllBytes("test_unknown_submessage_raw"));
        var out = new RtpsMessageReader().readRtpsMessage(buf);
        assertEquals(
                resourceUtils.readResource(getClass(), "test_unknown_submessage"), out.toString());
    }

    @Test
    public void test_read_sequenceNumber() throws Exception {
        var buf = ByteBuffer.wrap(XByte.copyToByteArray(-1, 0));
        var sq = new RtpsMessageReader().read(buf, SequenceNumber.class);
        System.out.println(sq);
        System.out.println(SequenceNumber.SEQUENCENUMBER_UNKNOWN);
        assertEquals(SequenceNumber.SEQUENCENUMBER_UNKNOWN, sq);
    }

    @ParameterizedTest
    @CsvSource({
        "00000000000000000000000000000000ffffffff00000000, ffffffff00000000",
        "00000000000000000000000000000000ffffffff00000001, ffffffff01000000"
    })
    public void test_read_sampleIdentity(String identityInput, String expectedSeqNum)
            throws Exception {
        Assertions.assertEquals(ByteOrder.LITTLE_ENDIAN, RtpsTalkConfiguration.getByteOrder());
        var buf = ByteBuffer.wrap(HexFormat.of().parseHex(identityInput));
        var identity = new RtpsMessageReader().read(buf, SampleIdentity.class);
        System.out.println(identity.sequenceNumber);
        assertEquals(HexFormat.fromHexDigitsToLong(expectedSeqNum), identity.sequenceNumber.value);
    }
}
