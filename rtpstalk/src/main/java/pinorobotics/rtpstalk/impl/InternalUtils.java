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

import id.xfunction.logging.XLogger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class InternalUtils {

    private static final XLogger LOGGER = XLogger.getLogger(InternalUtils.class);
    private static final InternalUtils INSTANCE = new InternalUtils();

    public static InternalUtils getInstance() {
        return INSTANCE;
    }

    public List<NetworkInterface> listAllNetworkInterfaces() {
        try {
            return NetworkInterface.networkInterfaces()
                    .filter(
                            p -> {
                                try {
                                    var hasIpv4 =
                                            p.inetAddresses()
                                                    .filter(isIpv4())
                                                    .findAny()
                                                    .isPresent();
                                    return p.isUp() && hasIpv4;
                                } catch (SocketException e) {
                                    LOGGER.severe(
                                            "Error reading network interface " + p.getDisplayName(),
                                            e);
                                }
                                return false;
                            })
                    .toList();
        } catch (SocketException e) {
            throw new RuntimeException("Error listing available network interfaces", e);
        }
    }

    public int padding(int len, int blockSize) {
        var padding = 0;
        if (len % blockSize != 0) {
            padding = blockSize - (len % 4);
        }
        return padding;
    }

    public static Predicate<InetAddress> isIpv4() {
        return addr -> addr instanceof Inet4Address;
    }
}
