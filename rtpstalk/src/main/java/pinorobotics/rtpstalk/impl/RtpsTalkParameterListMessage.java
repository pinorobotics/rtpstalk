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
package pinorobotics.rtpstalk.impl;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public record RtpsTalkParameterListMessage(ParameterList inlineQos, ParameterList parameterList)
        implements RtpsTalkMessage {
    public RtpsTalkParameterListMessage(ParameterList parameterList) {
        this(ParameterList.EMPTY, parameterList);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("inlineQos", inlineQos);
        builder.append("parameterList", parameterList);
        return builder.toString();
    }

    @Override
    public Parameters userInlineQos() {
        // this is internal message so it has no user parameters
        return Parameters.EMPTY;
    }
}
