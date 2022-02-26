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

import java.util.HashMap;
import java.util.Map;

/** @author aeon_flux aeon_flux@eclipso.ch */
public enum LocatorKind {
    LOCATOR_KIND_INVALID(-1),
    LOCATOR_KIND_RESERVED(0),
    LOCATOR_KIND_UDPv4(1),
    LOCATOR_KIND_UDPv6(2);

    public int value;

    public static Map<Integer, LocatorKind> VALUES = new HashMap<>();

    static {
        for (var t : values()) VALUES.put(t.value, t);
    }

    LocatorKind(int value) {
        this.value = value;
    }
}
