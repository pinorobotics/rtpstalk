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

import static java.util.stream.Collectors.toList;

import id.xfunction.logging.XLogger;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class InternalUtils {

    private static final InternalUtils INSTANCE = new InternalUtils();

    public static InternalUtils getInstance() {
        return INSTANCE;
    }

    public XLogger getLogger(Class<?> clazz, TracingToken token) {
        return XLogger.getLogger(clazz.getName() + "#" + token.toString());
    }

    public XLogger getLogger(Class<?> clazz, EntityId contextEntityId) {
        return XLogger.getLogger(clazz.getName() + "#" + contextEntityId.toString());
    }

    public List<NetworkInterface> listAllNetworkInterfaces() {
        try {
            return NetworkInterface.networkInterfaces().collect(toList());
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
}
