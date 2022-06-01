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
import pinorobotics.rtpstalk.messages.BuiltinEndpointQos;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.messages.DestinationOrderQosPolicy;
import pinorobotics.rtpstalk.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.messages.Duration;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.KeyHash;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.messages.StatusInfo;
import pinorobotics.rtpstalk.messages.UserDataQosPolicy;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.spec.RtpsSpecReference;

/** @author aeon_flux aeon_flux@eclipso.ch */
public enum ParameterId {
    PID_ENTITY_NAME(0x0062, String.class),
    PID_BUILTIN_ENDPOINT_SET(0x0058, BuiltinEndpointSet.class),
    PID_PARTICIPANT_LEASE_DURATION(0x0002, Duration.class),
    PID_DEFAULT_UNICAST_LOCATOR(0x0031, Locator.class),
    PID_UNICAST_LOCATOR(0x002f, Locator.class),
    PID_METATRAFFIC_UNICAST_LOCATOR(0x0032, Locator.class),
    PID_PARTICIPANT_GUID(0x0050, Guid.class),
    PID_VENDORID(0x0016, VendorId.class),
    PID_PROTOCOL_VERSION(0x0015, ProtocolVersion.class),
    PID_USER_DATA(0x002c, UserDataQosPolicy.class),
    PID_TOPIC_NAME(0x0005, String.class),
    PID_EXPECTS_INLINE_QOS(0x0043, Boolean.class),
    PID_TYPE_NAME(0x0007, String.class),
    PID_ENDPOINT_GUID(0x005a, Guid.class),
    PID_BUILTIN_ENDPOINT_QOS(0x0077, BuiltinEndpointQos.class),
    PID_KEY_HASH(0x0070, KeyHash.class),

    @RtpsSpecReference(
            paragraph = "8.7.2.2",
            protocolVersion = Predefined.Version_2_3,
            text = "DDS QoS Parameters that affect the wire protocol")
    PID_RELIABILITY(0x001a, ReliabilityQosPolicy.class),

    PID_STATUS_INFO(0x0071, StatusInfo.class),

    @RtpsSpecReference(
            paragraph = "8.7.2.2",
            protocolVersion = Predefined.Version_2_3,
            text = "DDS QoS Parameters that affect the wire protocol")
    PID_DESTINATION_ORDER(0x0025, DestinationOrderQosPolicy.class),

    @RtpsSpecReference(
            paragraph = "8.7.2.2",
            protocolVersion = Predefined.Version_2_3,
            text = "DDS QoS Parameters that affect the wire protocol")
    PID_DURABILITY(0x001d, DurabilityQosPolicy.class),
    PID_SENTINEL(0x0001, null);

    public static Map<Short, ParameterId> map = new HashMap<>();

    static {
        for (var t : ParameterId.values()) map.put(t.getValue(), t);
    }

    private short value;
    private Class<?> clazz;

    ParameterId(int i, Class<?> clazz) {
        this.clazz = clazz;
        this.value = (short) i;
    }

    public short getValue() {
        return value;
    }

    public Class<?> getParameterClass() {
        return clazz;
    }
}
