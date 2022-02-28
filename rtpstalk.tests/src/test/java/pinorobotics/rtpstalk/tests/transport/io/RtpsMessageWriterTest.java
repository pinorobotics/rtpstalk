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
package pinorobotics.rtpstalk.tests.transport.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import id.xfunction.io.XInputStream;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.transport.io.RtpsMessageWriter;

/** @author lambdaprime intid@protonmail.com */
public class RtpsMessageWriterTest {

    @ParameterizedTest
    @MethodSource("pinorobotics.rtpstalk.tests.transport.io.DataProviders#rtpsMessageConversion")
    public void testWrite(List testData) throws Exception {
        var expected = ByteBuffer.wrap(new XInputStream((String) testData.get(0)).readAllBytes());
        var actual = ByteBuffer.allocate(RtpsTalkConfiguration.DEFAULT.getPacketBufferSize());
        new RtpsMessageWriter().writeRtpsMessage((RtpsMessage) testData.get(1), actual);
        actual.limit(actual.position());
        actual.rewind();
        System.out.println(actual);
        assertEquals(expected, actual);
    }
}
