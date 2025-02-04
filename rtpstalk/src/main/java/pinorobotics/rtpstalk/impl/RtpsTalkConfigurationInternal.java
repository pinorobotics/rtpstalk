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
package pinorobotics.rtpstalk.impl;

import id.xfunction.Preconditions;
import id.xfunction.XJsonStringBuilder;
import id.xfunction.function.Unchecked;
import java.nio.ByteBuffer;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Header;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageReader;

/**
 * Provides additional internal parameters on top of {@link RtpsTalkConfiguration}. These are
 * calculated based on {@link RtpsTalkConfiguration}
 *
 * @author lambdaprime intid@protonmail.com
 */
public record RtpsTalkConfigurationInternal(
        RtpsTalkConfiguration publicConfig, int maxSubmessageSize, Guid localParticipantGuid) {
    private static final RtpsMessageReader MESSAGE_READER = new RtpsMessageReader();

    public RtpsTalkConfigurationInternal(RtpsTalkConfiguration config) {
        this(
                config,
                calcMaxSubmessageSize(config),
                Unchecked.get(
                        () ->
                                MESSAGE_READER.read(
                                        ByteBuffer.wrap(config.localParticipantGuid()),
                                        Guid.class)));
    }

    public RtpsTalkConfigurationInternal {
        Preconditions.isTrue(
                maxSubmessageSize > 0, "Unexpected maxMessageSize %s", maxSubmessageSize);
    }

    private static int calcMaxSubmessageSize(RtpsTalkConfiguration config) {
        var maxMessageSize =
                config.packetBufferSize()
                        - LengthCalculator.getInstance().getFixedLength(Header.class);
        LengthCalculator.getInstance().validateSubmessageSize(maxMessageSize);
        return maxMessageSize;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("config", publicConfig);
        builder.append("localParticpantGuid", localParticipantGuid);
        builder.append("maxSubmessageSize", maxSubmessageSize);
        return builder.toString();
    }
}
