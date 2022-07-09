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

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public record RtpsTalkDataMessage(Parameters inlineQos, byte[] data) implements RtpsTalkMessage {

    public RtpsTalkDataMessage(byte[] data) {
        this(Parameters.EMPTY, data);
    }

    public RtpsTalkDataMessage(String data) {
        this(data.getBytes());
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("inlineQos", inlineQos);
        builder.append("data", XByte.toHexPairs(data));
        return builder.toString();
    }
}
