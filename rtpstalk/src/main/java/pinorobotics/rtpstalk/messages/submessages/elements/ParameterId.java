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

import java.util.HashMap;
import java.util.Map;

/** @author aeon_flux aeon_flux@eclipso.ch */
public enum ParameterId {
    PID_ENTITY_NAME(0x0062),
    PID_BUILTIN_ENDPOINT_SET(0x0058),
    PID_PARTICIPANT_LEASE_DURATION(0x0002),
    PID_DEFAULT_UNICAST_LOCATOR(0x0031),
    PID_UNICAST_LOCATOR(0x002f),
    PID_METATRAFFIC_UNICAST_LOCATOR(0x0032),
    PID_PARTICIPANT_GUID(0x0050),
    PID_VENDORID(0x0016),
    PID_PROTOCOL_VERSION(0x0015),
    PID_USER_DATA(0x002c),
    PID_TOPIC_NAME(0x0005),
    PID_EXPECTS_INLINE_QOS(0x0043),
    PID_TYPE_NAME(0x0007),
    PID_ENDPOINT_GUID(0x005a),
    PID_BUILTIN_ENDPOINT_QOS(0x0077),
    PID_KEY_HASH(0x0070),
    PID_RELIABILITY(0x001a),
    PID_STATUS_INFO(0x0071),
    PID_DESTINATION_ORDER(0x0025),
    PID_SENTINEL(0x0001);

    public static Map<Short, ParameterId> map = new HashMap<>();

    static {
        for (var t : ParameterId.values()) map.put(t.getValue(), t);
    }

    private short value;

    ParameterId(int i) {
        this.value = (short) i;
    }

    public short getValue() {
        return value;
    }
}
