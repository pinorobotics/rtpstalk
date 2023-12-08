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
package pinorobotics.rtpstalk.impl.messages;

import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.Header;
import pinorobotics.rtpstalk.impl.spec.messages.ProtocolId;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Submessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;

/**
 * Aggregates submessages into single message until it becomes full
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsMessageAggregator {
    private static final int headerLen =
            LengthCalculator.getInstance().getFixedLength(Header.class);
    private XLogger logger;
    private List<Submessage> submessages = new ArrayList<Submessage>();
    private int messageLen = headerLen;
    private int maxSubmessageSize;
    private Header header;

    public RtpsMessageAggregator(
            TracingToken tracingToken, GuidPrefix writerGuidPrefix, int maxSubmessageSize) {
        this.maxSubmessageSize = maxSubmessageSize;
        this.header =
                new Header(
                        ProtocolId.Predefined.RTPS.getValue(),
                        ProtocolVersion.Predefined.Version_2_3.getValue(),
                        VendorId.Predefined.RTPSTALK.getValue(),
                        writerGuidPrefix);
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    public int getSize() {
        return messageLen;
    }

    public boolean add(Submessage submessage) {
        var submessageLen = submessage.submessageHeader.submessageLength.getUnsigned();
        if (messageLen + submessageLen > maxSubmessageSize) {
            logger.fine(
                    "Not enough space to add new submessage with size {0}. Current size {1},"
                            + " max size {2}",
                    submessageLen, messageLen, maxSubmessageSize);
            return false;
        }
        submessages.add(submessage);
        messageLen += submessageLen;
        return true;
    }

    public Optional<RtpsMessage> build() {
        return submessages.isEmpty()
                ? Optional.empty()
                : Optional.of(new RtpsMessage(header, submessages));
    }
}
