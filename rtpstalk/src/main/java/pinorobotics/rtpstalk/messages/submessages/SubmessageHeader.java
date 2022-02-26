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
package pinorobotics.rtpstalk.messages.submessages;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class SubmessageHeader {

    /** Identifies the kind of Submessage. */
    public SubmessageKind submessageKind;

    public byte submessageFlag;

    /** octetsToNextHeader */
    public short submessageLength;

    public SubmessageHeader() {}

    public SubmessageHeader(SubmessageKind kind, int submessageLength) {
        this(kind, RtpsTalkConfiguration.ENDIANESS_BIT, submessageLength);
    }

    public SubmessageHeader(SubmessageKind kind, int submessageFlag, int submessageLength) {
        this.submessageKind = kind;
        this.submessageFlag = (byte) submessageFlag;
        this.submessageLength = (short) submessageLength;
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
