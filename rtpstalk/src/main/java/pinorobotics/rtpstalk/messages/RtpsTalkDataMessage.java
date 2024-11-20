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
package pinorobotics.rtpstalk.messages;

import id.xfunction.XByte;
import id.xfunction.XJsonStringBuilder;
import java.util.Optional;

/**
 * Data message definition.
 *
 * <p>This is main type of messages in RTPS which is used to transfer user data.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public record RtpsTalkDataMessage(Optional<Parameters> userInlineQos, Optional<byte[]> data)
        implements RtpsTalkMessage {

    public RtpsTalkDataMessage(Parameters inlineQos, byte[] data) {
        this(Optional.of(inlineQos), Optional.of(data));
    }

    public RtpsTalkDataMessage(Optional<Parameters> inlineQos, byte[] data) {
        this(inlineQos, Optional.of(data));
    }

    public RtpsTalkDataMessage(Parameters inlineQos) {
        this(Optional.of(inlineQos), Optional.empty());
    }

    public RtpsTalkDataMessage(byte[] data) {
        this(Optional.empty(), Optional.of(data));
    }

    public RtpsTalkDataMessage(String data) {
        this(data.getBytes());
    }

    /** RTPS inline QoS to be included with a data message */
    @Override
    public Optional<Parameters> userInlineQos() {
        return userInlineQos;
    }

    /** Transfered user data */
    public Optional<byte[]> data() {
        return data;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("inlineQos", userInlineQos);
        builder.append("data", data.map(XByte::toHexPairs));
        return builder.toString();
    }
}
