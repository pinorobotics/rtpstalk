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
package pinorobotics.rtpstalk.impl.spec.discovery.spdp;

import static pinorobotics.rtpstalk.impl.spec.messages.TrafficType.*;

import pinorobotics.rtpstalk.impl.spec.messages.TrafficType;

/** @author aeon_flux aeon_flux@eclipso.ch */
public record PortNumberParameters(
        int DomainIdGain,
        int ParticipantIdGain,
        int PortBase,
        int d0,
        int d1,
        int d2,
        int d3,
        TrafficType trafficType) {

    public static final PortNumberParameters DEFAULT =
            new PortNumberParameters(250, 2, 7400, 0, 10, 1, 11, DISCOVERY);

    public int getMultiCastPort(int domainId) {
        return switch (trafficType) {
            case DISCOVERY -> PortBase + DomainIdGain * domainId + d0;
            case USER -> PortBase + DomainIdGain * domainId + d2;
        };
    }
}
