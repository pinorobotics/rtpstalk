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

import id.xfunction.Preconditions;
import id.xfunction.XJsonStringBuilder;
import id.xfunction.function.ConstantSupplier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.function.Supplier;
import pinorobotics.rtpstalk.discovery.spdp.PortNumberParameters;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class Locator {

    public static final Locator EMPTY_IPV6 = createEmpty(LocatorKind.LOCATOR_KIND_UDPv6);
    public static final Locator INVALID = createEmpty(LocatorKind.LOCATOR_KIND_INVALID);

    private LocatorKind kind;

    /**
     * Allows to assign port number later in time before it is actually used.
     *
     * <p>It avoids situations when it is assigned and before it is effectively being used some
     * other application already takes it.
     *
     * <p>Mainly it is needed for local Locators like defaultUnicastLocator,
     * metatrafficUnicastLocator.
     */
    private Supplier<Integer> portSupplier;

    private InetAddress address;
    private Optional<NetworkInterface> networkInterface = Optional.empty();

    public Locator(
            LocatorKind kind,
            Supplier<Integer> port,
            InetAddress address,
            NetworkInterface networkInterface) {
        this.kind = kind;
        portSupplier = port;
        this.address = address;
        this.networkInterface = Optional.ofNullable(networkInterface);
    }

    public Locator(
            LocatorKind kind, int port, InetAddress address, NetworkInterface networkInterface) {
        this(kind, new ConstantSupplier<>(port), address, networkInterface);
    }

    public Locator(LocatorKind kind, Supplier<Integer> port, InetAddress address) {
        this(kind, port, address, null);
        Preconditions.isTrue(
                !address.isMulticastAddress(),
                "This constructor does not support multicast addresses");
    }

    public Locator(LocatorKind kind, int port, InetAddress address) {
        this(kind, new ConstantSupplier<>(port), address);
    }

    public SocketAddress getSocketAddress() {
        return new InetSocketAddress(address, portSupplier.get());
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("transportType", kind);
        builder.append("port", portSupplier);
        builder.append("address", address);
        return builder.toString();
    }

    public static Locator createDefaultMulticastLocator(
            int domainId, NetworkInterface networkInterface) {
        try {
            return new Locator(
                    LocatorKind.LOCATOR_KIND_UDPv4,
                    PortNumberParameters.DEFAULT.getMultiCastPort(domainId),
                    InetAddress.getByName("239.255.0.1"),
                    networkInterface);
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

    /** Used in multicast locators */
    public Optional<NetworkInterface> networkInterface() {
        return networkInterface;
    }

    public LocatorKind kind() {
        return kind;
    }

    public int port() {
        return portSupplier.get();
    }
}
