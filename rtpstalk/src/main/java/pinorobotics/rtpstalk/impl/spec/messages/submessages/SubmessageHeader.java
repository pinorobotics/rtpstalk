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
package pinorobotics.rtpstalk.impl.spec.messages.submessages;

import id.xfunction.XJsonStringBuilder;
import java.util.List;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;
import pinorobotics.rtpstalk.impl.spec.messages.UnsignedShort;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SubmessageHeader implements HasStreamedFields {
    static final List<String> STREAMED_FIELDS =
            List.of("submessageKind", "submessageFlag", "submessageLength");

    /** Identifies the kind of Submessage. */
    public SubmessageKind submessageKind;

    public byte submessageFlag;

    /**
     * Number of octets from the start of the contents of the currennt Submessage to the start of
     * the next Submessage header.
     */
    public UnsignedShort submessageLength;

    public SubmessageHeader() {}

    public SubmessageHeader(SubmessageKind kind, int submessageLength) {
        this(kind, RtpsTalkConfiguration.ENDIANESS_BIT, submessageLength);
    }

    public SubmessageHeader(SubmessageKind kind, int submessageFlag, int submessageLength) {
        this.submessageKind = kind;
        this.submessageFlag = (byte) submessageFlag;
        this.submessageLength = new UnsignedShort(submessageLength);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("submessageKind", submessageKind);
        builder.append("submessageFlag", submessageFlag);
        builder.append("submessageLength", submessageLength);
        return builder.toString();
    }
}
