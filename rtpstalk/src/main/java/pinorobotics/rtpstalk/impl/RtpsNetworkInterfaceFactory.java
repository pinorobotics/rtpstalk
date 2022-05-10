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

import id.xfunction.Preconditions;
import id.xfunction.function.Unchecked;
import id.xfunction.lang.XRE;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.LocatorKind;
import pinorobotics.rtpstalk.transport.DataChannelFactory;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class RtpsNetworkInterfaceFactory {
    private RtpsTalkConfiguration config;
    private DataChannelFactory channelFactory;

    public RtpsNetworkInterfaceFactory(
            RtpsTalkConfiguration config, DataChannelFactory channelFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
    }

    public RtpsNetworkInterface createRtpsNetworkInterface(TracingToken tracingToken)
            throws IOException {

        var bindAddress =
                config.networkInterface().map(RtpsNetworkInterfaceFactory::getNetworkIfaceIp);
        var locatorAddress = bindAddress.orElseGet(this::decideLocatorAddress);

        // Assign port numbers right before starting the services.
        // It avoids situations when it is assigned too early and before it is
        // effectively being used some other application already takes it.
        // Mainly it is needed for local Locators like defaultUnicastLocator,
        // metatrafficUnicastLocator.
        var builtinDataChannel =
                channelFactory.bind(tracingToken, bindAddress, config.builtInEnpointsPort());
        var builtinLocator =
                new Locator(
                        LocatorKind.LOCATOR_KIND_UDPv4,
                        builtinDataChannel.getLocalPort(),
                        locatorAddress);
        var userDataChannel =
                channelFactory.bind(tracingToken, bindAddress, config.userEndpointsPort());
        var userDataLocator =
                new Locator(
                        LocatorKind.LOCATOR_KIND_UDPv4,
                        userDataChannel.getLocalPort(),
                        locatorAddress);

        return new RtpsNetworkInterface(
                userDataChannel, userDataLocator, builtinDataChannel, builtinLocator);
    }

    private static InetAddress getNetworkIfaceIp(NetworkInterface networkIface) {
        try {
            return networkIface.getInterfaceAddresses().get(0).getAddress();
        } catch (Exception e) {
            throw new XRE("Error obtaining IP address for network interface %s", networkIface);
        }
    }

    private InetAddress decideLocatorAddress() {
        var ifaces = InternalUtils.getInstance().listAllNetworkInterfaces();
        Preconditions.isTrue(!ifaces.isEmpty(), "No network interfaces found");
        NetworkInterface iface = null;
        if (ifaces.size() == 1) {
            iface = ifaces.get(0);
        } else {
            iface =
                    ifaces.stream()
                            .filter(ni -> Unchecked.getBoolean(() -> !ni.isLoopback()))
                            .findFirst()
                            .orElse(ifaces.get(0));
        }
        return getNetworkIfaceIp(iface);
    }
}
