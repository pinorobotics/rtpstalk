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
package pinorobotics.rtpstalk;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides additional information on the QoS of the built-in Endpoints supported by the
 * Participant.
 *
 * @author lambdaprime intid@protonmail.com
 */
public enum EndpointQos {
    BEST_EFFORT_PARTICIPANT_MESSAGE_DATA_READER(0),
    NONE(-1);

    private static final Map<Integer, EndpointQos> MAP =
            Arrays.stream(EndpointQos.values()).collect(Collectors.toMap(k -> k.value, v -> v));
    private int value;

    EndpointQos(int position) {
        this.value = position;
    }

    public int getValue() {
        return value;
    }

    /** Return {@link EndpointQos} by its integer value or NONE if it does not exist */
    public static EndpointQos valueOf(int value) {
        return MAP.getOrDefault(value, NONE);
    }
}
