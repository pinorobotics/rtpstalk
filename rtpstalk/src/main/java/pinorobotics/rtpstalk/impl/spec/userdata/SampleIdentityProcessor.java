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

import static pinorobotics.rtpstalk.messages.UserParameterId.PID_FASTDDS_SAMPLE_IDENTITY;
import static pinorobotics.rtpstalk.messages.UserParameterId.PID_RELATED_SAMPLE_IDENTITY;

import id.xfunction.logging.XLogger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import pinorobotics.rtpstalk.impl.spec.messages.SampleIdentity;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageReader;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageWriter;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class SampleIdentityProcessor {
    private static final XLogger LOGGER = XLogger.getLogger(SampleIdentityProcessor.class);
    private static final RtpsMessageReader MESSAGE_READER = new RtpsMessageReader();
    private static final RtpsMessageWriter MESSAGE_WRITER = new RtpsMessageWriter();

    /**
     * Update sample identities {@link SequenceNumber#SEQUENCENUMBER_UNKNOWN} with actual sequence
     * number of the RTPS message
     */
    public void updateSampleIdentity(Parameters params, SequenceNumber seqNum) {
        updateSampleIdentity(params, seqNum, PID_RELATED_SAMPLE_IDENTITY);
        updateSampleIdentity(params, seqNum, PID_FASTDDS_SAMPLE_IDENTITY);
    }

    private void updateSampleIdentity(
            Parameters params, SequenceNumber seqNum, short sampleIdentityId) {
        var identityInfo =
                findSampleIdentity(params.getParameters(), sampleIdentityId).orElse(null);
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
     * Send DATA to the Reader only when Reader's guid matches with any of the sample identity
     * assigned to the DATA or when no sample identity is present inside the message, otherwise send
     * GAP
     */
    public boolean shouldReplaceWithGap(
            RtpsTalkDataMessage message, GuidPrefix readerGuidPrefix, GuidPrefix writerGuidPrefix) {
        var parameters = message.userInlineQos().map(Parameters::getParameters).orElse(Map.of());
        if (parameters.isEmpty()) return false;
        var relatedIdentity =
                verifyIdentity(
                        parameters,
                        PID_RELATED_SAMPLE_IDENTITY,
                        readerGuidPrefix,
                        writerGuidPrefix);
        if (relatedIdentity == Result.MATCH) return false;
        var fastDdsIdentity =
                verifyIdentity(
                        parameters,
                        PID_FASTDDS_SAMPLE_IDENTITY,
                        readerGuidPrefix,
                        writerGuidPrefix);
        if (fastDdsIdentity == Result.MATCH) return false;
        return (relatedIdentity == fastDdsIdentity && relatedIdentity == Result.NO_MATCH);
    }

    private enum Result {
        NO_IDENTITY,
        MATCH,
        NO_MATCH
    }

    /** Check if any of GuidPrefix matches specified identity */
    private Result verifyIdentity(
            Map<Short, byte[]> parameters, short sampleIdentityId, GuidPrefix... guidPrefix) {
        var identity =
                findSampleIdentity(parameters, sampleIdentityId)
                        .map(SampleIdentityInfo::identity)
                        .orElse(null);
        if (identity == null) return Result.NO_IDENTITY;
        var identityWriterGuidPrefix = identity.getWriterGuid().guidPrefix;
        return Arrays.stream(guidPrefix)
                        .filter(Predicate.isEqual(identityWriterGuidPrefix))
                        .findFirst()
                        .isPresent()
                ? Result.MATCH
                : Result.NO_MATCH;
    }

    private record SampleIdentityInfo(SampleIdentity identity, byte[] buffer) {}

    private Optional<SampleIdentityInfo> findSampleIdentity(
            Map<Short, byte[]> parameters, short sampleIdentityId) {
        var buf = parameters.get(sampleIdentityId);
        if (buf == null || buf.length != SampleIdentity.SIZE) return Optional.empty();
        try {
            var identityBuf = ByteBuffer.wrap(buf);
            return Optional.of(
                    new SampleIdentityInfo(
                            MESSAGE_READER.read(identityBuf, SampleIdentity.class), buf));
        } catch (Exception e) {
            // not a sample identity, ignoring
            return Optional.empty();
        }
    }
}
