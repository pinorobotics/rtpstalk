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
package pinorobotics.rtpstalk.impl.spec.messages;

/** identifying the kind of transport */
/** @author aeon_flux aeon_flux@eclipso.ch */
public enum TransportType {
    LOCATOR_KIND_INVALID(-1),
    LOCATOR_KIND_RESERVED(0),
    LOCATOR_KIND_UDPv4(1),
    LOCATOR_KIND_UDPv6(2);

    private int val;

    private TransportType(int val) {
        this.val = val;
    }

    public int getValue() {
        return val;
    }
}
