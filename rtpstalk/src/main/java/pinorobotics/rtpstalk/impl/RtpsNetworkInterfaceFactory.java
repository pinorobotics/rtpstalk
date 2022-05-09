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

import id.xfunction.net.FreePortIterator;
import id.xfunction.net.FreePortIterator.Protocol;
import java.net.NetworkInterface;
import java.util.Optional;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class RtpsNetworkInterfaceFactory {

    private RtpsTalkConfiguration config;
    private Optional<Integer> builtInEnpointsPort = Optional.empty();
    private Optional<Integer> userEndpointsPort = Optional.empty();

    public RtpsNetworkInterfaceFactory(RtpsTalkConfiguration config) {
        this.config = config;
    }

    public RtpsNetworkInterface createRtpsNetworkInterface(NetworkInterface iface) {
        var portIterator = new FreePortIterator(config.startPort(), Protocol.UDP);

        // looks like FASTRTPS does not support participant which runs on multiple network
        // interfaces on different ports
        // for example: lo 127.0.0.1 (7414, 7415), eth 172.17.0.2 (7412, 7413)
        // in that case it will be sending messages to lo 127.0.0.1 (7412, 7413) which is wrong
        // possibly it is expected or may be it is FASTRTPS bug but to make it work we
        // disallow support of multiple network interfaces on different ports and assign them only
        // once and for all network interfaces
        if (builtInEnpointsPort.isEmpty())
            builtInEnpointsPort =
                    Optional.of(config.builtInEnpointsPort().orElseGet(() -> portIterator.next()));
        if (userEndpointsPort.isEmpty())
            userEndpointsPort =
                    Optional.of(config.userEndpointsPort().orElseGet(() -> portIterator.next()));
        return new RtpsNetworkInterface(
                config.domainId(), iface, builtInEnpointsPort.get(), userEndpointsPort.get());
    }
}
