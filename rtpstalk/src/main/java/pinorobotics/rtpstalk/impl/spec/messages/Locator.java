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

import id.xfunction.Preconditions;
import id.xfunction.XJsonStringBuilder;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.spec.discovery.spdp.PortNumberParameters;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class Locator {

    public static final Locator EMPTY_IPV6 = createEmpty(LocatorKind.LOCATOR_KIND_UDPv6);
    public static final Locator INVALID = createEmpty(LocatorKind.LOCATOR_KIND_INVALID);

    private LocatorKind kind;
    private UnsignedInt port;
    private InetAddress address;
    private InetSocketAddress socketAddress;

    public Locator(LocatorKind kind, int port, InetAddress address) {
        if (kind == LocatorKind.LOCATOR_KIND_UDPv4)
            Preconditions.isTrue(
                    InternalUtils.isIpv4().test(address), "Non IPv4 address %s", address);
        this.kind = kind;
        this.port = new UnsignedInt(port);
        this.address = address;
        socketAddress = new InetSocketAddress(address, port);
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
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

    public InetAddress address() {
        return address;
    }

    public LocatorKind kind() {
        return kind;
    }

    public long port() {
        return port.getUnsigned();
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, kind, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Locator other = (Locator) obj;
        return Objects.equals(address, other.address)
                && kind == other.kind
                && Objects.equals(port, other.port);
    }
}
