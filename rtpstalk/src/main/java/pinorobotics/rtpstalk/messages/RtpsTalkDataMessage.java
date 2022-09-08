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
public class RtpsTalkDataMessage implements RtpsTalkMessage {

    private Optional<Parameters> inlineQos = Optional.empty();
    private Optional<byte[]> data = Optional.empty();

    public RtpsTalkDataMessage(Parameters inlineQos, byte[] data) {
        this.inlineQos = Optional.ofNullable(inlineQos);
        this.data = Optional.ofNullable(data);
    }

    public RtpsTalkDataMessage(Optional<Parameters> inlineQos, byte[] data) {
        this.inlineQos = inlineQos;
        this.data = Optional.ofNullable(data);
    }

    public RtpsTalkDataMessage(Parameters inlineQos) {
        this(inlineQos, null);
    }

    public RtpsTalkDataMessage(byte[] data) {
        this(Optional.empty(), data);
    }

    public RtpsTalkDataMessage(String data) {
        this(data.getBytes());
    }

    /** RTPS inline QoS to be included with a data message */
    @Override
    public Optional<Parameters> userInlineQos() {
        return inlineQos;
    }

    /** Transfered user data */
    public Optional<byte[]> data() {
        return data;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("inlineQos", inlineQos);
        builder.append("data", data.map(XByte::toHexPairs));
        return builder.toString();
    }
}
