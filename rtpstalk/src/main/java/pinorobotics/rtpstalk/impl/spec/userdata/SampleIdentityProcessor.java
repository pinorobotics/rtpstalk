/*
 * Copyright 2024 pinorobotics
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
package pinorobotics.rtpstalk.impl.spec.userdata;

import id.xfunction.logging.XLogger;
import java.nio.ByteBuffer;
import java.util.Objects;
import pinorobotics.rtpstalk.impl.spec.messages.SampleIdentity;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageReader;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageWriter;
import pinorobotics.rtpstalk.messages.Parameters;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class SampleIdentityProcessor {
    private static final XLogger LOGGER = XLogger.getLogger(SampleIdentityProcessor.class);
    private static final RtpsMessageReader MESSAGE_READER = new RtpsMessageReader();
    private static final RtpsMessageWriter MESSAGE_WRITER = new RtpsMessageWriter();

    public void updateSampleIdentity(Parameters params, SequenceNumber seqNum) {
        var buf = params.getParameters().get(ParameterId.NonRtps.PID_FASTDDS_SAMPLE_IDENTITY);
        try {
            var identityBuf = ByteBuffer.wrap(buf);
            var identity = MESSAGE_READER.read(identityBuf, SampleIdentity.class);
            if (!Objects.equals(identity.sequenceNumber, SequenceNumber.SEQUENCENUMBER_UNKNOWN))
                return;
            LOGGER.fine("Update sample identity");
            identity.sequenceNumber = seqNum;
            identityBuf.rewind();
            MESSAGE_WRITER.write(identity, identityBuf);
        } catch (Exception e) {
            // non FastDDS sample identity, ignoring
        }
    }
}
