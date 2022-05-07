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

import id.xfunction.function.LazyInitializer;
import id.xfunction.net.FreePortIterator;
import id.xfunction.net.FreePortIterator.Protocol;
import java.net.NetworkInterface;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class RtpsNetworkInterfaceFactory {

    private RtpsTalkConfiguration config;

    public RtpsNetworkInterfaceFactory(RtpsTalkConfiguration config) {
        this.config = config;
    }

    public RtpsNetworkInterface createRtpsNetworkInterface(NetworkInterface iface) {
        var portIterator = new FreePortIterator(config.startPort(), Protocol.UDP);
        var builtInEnpointsPortSupplier =
                new LazyInitializer<Integer>(
                        () -> config.builtInEnpointsPort().orElseGet(() -> portIterator.next()));
        var userEndpointsPortSupplier =
                new LazyInitializer<Integer>(
                        () -> config.userEndpointsPort().orElseGet(() -> portIterator.next()));
        return new RtpsNetworkInterface(
                config.domainId(), iface, builtInEnpointsPortSupplier, userEndpointsPortSupplier);
    }
}
