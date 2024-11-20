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

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageReader;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageWriter;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsMessageTransportTest {

    @Test
    public void test_bigData() throws Exception {
        var bigData = new byte[64_000];
        var expected =
                new RtpsMessage(
                        TestConstants.TEST_HEADER,
                        new Data(
                                new EntityId(0x12, EntityKind.READER_NO_KEY),
                                new EntityId(0x01, EntityKind.WRITER_NO_KEY),
                                new SequenceNumber(1),
                                new SerializedPayload(new RawData(bigData), true)));
        var buf = ByteBuffer.allocate(TestConstants.TEST_CONFIG.packetBufferSize());
        new RtpsMessageWriter().writeRtpsMessage(expected, buf);
        buf.limit(buf.position());
        buf.rewind();
        var actual = new RtpsMessageReader().readRtpsMessage(buf).get();
        Assertions.assertEquals(expected.toString(), actual.toString());
    }
}
