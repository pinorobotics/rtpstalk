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

import id.xfunction.XJsonStringBuilder;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public record RtpsTalkParameterListMessage(
        Optional<ParameterList> inlineQos, Optional<ParameterList> parameterList)
        implements RtpsTalkMessage {

    public RtpsTalkParameterListMessage(ParameterList inlineQos, ParameterList parameterList) {
        this(Optional.of(inlineQos), Optional.of(parameterList));
    }

    public RtpsTalkParameterListMessage(ParameterList parameterList) {
        this(Optional.empty(), Optional.of(parameterList));
    }

    public static RtpsTalkParameterListMessage withInlineQosOnly(ParameterList inlineQos) {
        return new RtpsTalkParameterListMessage(Optional.of(inlineQos), Optional.empty());
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("inlineQos", inlineQos);
        builder.append("parameterList", parameterList);
        return builder.toString();
    }

    @Override
    public Optional<Parameters> userInlineQos() {
        return Optional.empty();
    }

    public Optional<ParameterList> inlineQos() {
        return inlineQos;
    }

    public Optional<ParameterList> parameterList() {
        return parameterList;
    }
}
