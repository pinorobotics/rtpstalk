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
package pinorobotics.rtpstalk.impl.spec.messages.submessages.elements;

import id.xfunction.XJsonStringBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Payload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RepresentationIdentifier;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RepresentationIdentifier.Predefined;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ParameterList implements SubmessageElement, Payload {

    private Map<ParameterId, Object> params = new LinkedHashMap<>();
    private Map<Short, byte[]> userParams = new LinkedHashMap<>();

    public ParameterList() {}

    public ParameterList(Map<Short, byte[]> userParams) {
        this.userParams = userParams;
    }

    public Map<ParameterId, Object> getParameters() {
        return params;
    }

    public Map<Short, byte[]> getUserParameters() {
        return userParams;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("params", params);
        builder.append("userParams", userParams);
        return builder.toString();
    }

    @Override
    public Predefined getRepresentationIdentifier() {
        return RepresentationIdentifier.Predefined.PL_CDR_LE;
    }

    public void put(ParameterId parameterId, Object value) {
        params.putIfAbsent(parameterId, value);
    }

    public void putUserParameter(short parameterId, byte[] value) {
        userParams.putIfAbsent(parameterId, value);
    }

    public boolean isEmpty() {
        return params.isEmpty() && userParams.isEmpty();
    }
}
