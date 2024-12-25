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
import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.SampleIdentity;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageReader;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageWriter;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.messages.UserParameterId;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class SampleIdentityProcessor {
    private static final XLogger LOGGER = XLogger.getLogger(SampleIdentityProcessor.class);
    private static final RtpsMessageReader MESSAGE_READER = new RtpsMessageReader();
    private static final RtpsMessageWriter MESSAGE_WRITER = new RtpsMessageWriter();

    public void updateSampleIdentity(Parameters params, SequenceNumber seqNum) {
        var identityInfo = findSampleIdentity(params).orElse(null);
        if (identityInfo == null) return;
        var identity = identityInfo.identity;
        if (!Objects.equals(identity.sequenceNumber, SequenceNumber.SEQUENCENUMBER_UNKNOWN)) return;
        try {
            LOGGER.fine("Update sample identity");
            identity.sequenceNumber = seqNum;
            var identityBuf = ByteBuffer.wrap(identityInfo.buffer);
            MESSAGE_WRITER.write(identity, identityBuf);
        } catch (Exception e) {
            LOGGER.severe("Failed to update sample identity: {0}", e.getMessage());
        }
    }

    /**
     * Send DATA to the Reader only when Reader's guid matches with sample identity assigned to the
     * DATA, otherwise send GAP
     */
    public boolean shouldReplaceWithGap(
            RtpsTalkDataMessage message, GuidPrefix readerGuidPrefix, GuidPrefix writerGuidPrefix) {
        var identity =
                findSampleIdentity(message.userInlineQos().orElse(Parameters.EMPTY))
                        .map(SampleIdentityInfo::identity)
                        .orElse(null);
        if (identity == null) return false;
        var identityWriterGuidPrefix = identity.getWriterGuid().guidPrefix;
        if (Objects.equals(identityWriterGuidPrefix, writerGuidPrefix)) return false;
        if (Objects.equals(identityWriterGuidPrefix, readerGuidPrefix)) return false;
        return true;
    }

    private record SampleIdentityInfo(SampleIdentity identity, byte[] buffer) {}

    private Optional<SampleIdentityInfo> findSampleIdentity(Parameters params) {
        var buf = params.getParameters().get(UserParameterId.PID_FASTDDS_SAMPLE_IDENTITY);
        if (buf == null || buf.length != SampleIdentity.SIZE) return Optional.empty();
        try {
            var identityBuf = ByteBuffer.wrap(buf);
            return Optional.of(
                    new SampleIdentityInfo(
                            MESSAGE_READER.read(identityBuf, SampleIdentity.class), buf));
        } catch (Exception e) {
            // non FastDDS sample identity, ignoring
            return Optional.empty();
        }
    }
}
