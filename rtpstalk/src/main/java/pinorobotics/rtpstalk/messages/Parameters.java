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

import id.xfunction.XJsonStringBuilder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RTPS user defined parameters (part of inline QoS)
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class Parameters {

    /** Empty parameters object */
    public static final Parameters EMPTY = new Parameters();

    private Map<Short, byte[]> params = new LinkedHashMap<>();

    public Parameters() {}

    /**
     * @param params parameters to add
     */
    public Parameters(Map<Short, byte[]> params) {
        this.params = new LinkedHashMap<>(params);
    }

    public Map<Short, byte[]> getParameters() {
        return params;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("params", params);
        return builder.toString();
    }
}
