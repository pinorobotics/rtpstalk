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
package pinorobotics.rtpstalk;

import id.xfunction.XJsonStringBuilder;
import id.xfunction.lang.XRE;
import java.net.InetAddress;
import java.net.NetworkInterface;
import pinorobotics.rtpstalk.behavior.OperatingEntities;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.LocatorKind;

/** @author lambdaprime intid@protonmail.com */
public class RtpsNetworkInterface {

    private Locator defaultUnicastLocator;
    private Locator metatrafficUnicastLocator;
    private Locator metatrafficMulticastLocator;
    private InetAddress ipAddress;
    private NetworkInterface networkIface;
    private OperatingEntities operatingEntities = new OperatingEntities();

    public RtpsNetworkInterface(
            int domainId,
            NetworkInterface networkIface,
            int builtInEnpointsPort,
            int userEndpointsPort) {
        this.networkIface = networkIface;
        ipAddress = getNetworkIfaceIp(networkIface);
        defaultUnicastLocator =
                new Locator(LocatorKind.LOCATOR_KIND_UDPv4, userEndpointsPort, ipAddress);
        metatrafficUnicastLocator =
                new Locator(LocatorKind.LOCATOR_KIND_UDPv4, builtInEnpointsPort, ipAddress);
        metatrafficMulticastLocator = Locator.createDefaultMulticastLocator(domainId, networkIface);
    }

    public RtpsNetworkInterface withBuiltinEndpointPort(int builtInEnpointsPort) {
        metatrafficUnicastLocator =
                new Locator(LocatorKind.LOCATOR_KIND_UDPv4, builtInEnpointsPort, ipAddress);
        return this;
    }

    public RtpsNetworkInterface withUserEndpointPort(int userEndpointsPort) {
        defaultUnicastLocator =
                new Locator(LocatorKind.LOCATOR_KIND_UDPv4, userEndpointsPort, ipAddress);
        return this;
    }

    public Locator getLocalDefaultUnicastLocator() {
        return defaultUnicastLocator;
    }

    public Locator getLocalMetatrafficUnicastLocator() {
        return metatrafficUnicastLocator;
    }

    public Locator getLocalMetatrafficMulticastLocator() {
        return metatrafficMulticastLocator;
    }

    public NetworkInterface getNetworkIface() {
        return networkIface;
    }

    public InetAddress getLocalIpAddress() {
        return ipAddress;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("ipAddress", ipAddress);
        builder.append("builtInEnpointsPort", metatrafficUnicastLocator.port());
        builder.append("userEndpointsPort", defaultUnicastLocator.port());
        return builder.toString();
    }

    private static InetAddress getNetworkIfaceIp(NetworkInterface networkIface) {
        try {
            return networkIface.getInterfaceAddresses().get(0).getAddress();
        } catch (Exception e) {
            throw new XRE("Error obtaining IP address for network interface %s", networkIface);
        }
    }
}
