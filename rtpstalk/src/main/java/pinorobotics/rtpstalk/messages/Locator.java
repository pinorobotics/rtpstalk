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

import id.xfunction.XJsonStringBuilder;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import pinorobotics.rtpstalk.discovery.spdp.PortNumberParameters;

/** @author aeon_flux aeon_flux@eclipso.ch */
public record Locator(LocatorKind kind, int port, InetAddress address) {

    public static final Locator EMPTY_IPV6 = createEmpty(LocatorKind.LOCATOR_KIND_UDPv6);
    public static final Locator INVALID = createEmpty(LocatorKind.LOCATOR_KIND_INVALID);

    public SocketAddress getSocketAddress() {
        return new InetSocketAddress(address, port);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("transportType", kind);
        builder.append("port", port);
        builder.append("address", address);
        return builder.toString();
    }

    public static Locator createDefaultMulticastLocator(int domainId) {
        try {
            return new Locator(
                    LocatorKind.LOCATOR_KIND_UDPv4,
                    PortNumberParameters.DEFAULT.getMultiCastPort(domainId),
                    InetAddress.getByName("239.255.0.1"));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static Locator createEmpty(LocatorKind kind) {
        try {
            return new Locator(kind, 0, InetAddress.getByAddress(new byte[4]));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
