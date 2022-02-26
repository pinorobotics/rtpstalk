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
package pinorobotics.rtpstalk.messages.submessages.elements;

import id.xfunction.XJsonStringBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import pinorobotics.rtpstalk.messages.submessages.Payload;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class ParameterList implements SubmessageElement, Payload {

    public Map<ParameterId, Object> params = new LinkedHashMap<>();

    public ParameterList() {}

    public ParameterList(LinkedHashMap<ParameterId, Object> params) {
        this.params = params;
    }

    public ParameterList(List<Entry<ParameterId, Object>> entries) {
        entries.stream().forEach(e -> params.put(e.getKey(), e.getValue()));
    }

    public Map<ParameterId, ?> getParameters() {
        return params;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("params", params);
        return builder.toString();
    }
}
