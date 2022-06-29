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

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class TracingToken {

    private static final String DELIM = "-";
    private String token;

    public TracingToken(String... tokens) {
        this.token = Arrays.stream(tokens).collect(Collectors.joining(DELIM));
    }

    public TracingToken(TracingToken token, String... tokens) {
        this.token = token + DELIM + Arrays.stream(tokens).collect(Collectors.joining(DELIM));
    }

    @Override
    public String toString() {
        return token;
    }
}
