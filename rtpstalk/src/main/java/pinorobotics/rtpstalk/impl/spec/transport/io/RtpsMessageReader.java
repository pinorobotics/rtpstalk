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
package pinorobotics.rtpstalk.impl.spec.transport.io;

import id.kineticstreamer.KineticStreamReader;
import id.xfunction.logging.XLogger;
import java.nio.ByteBuffer;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.transport.io.exceptions.NotRtpsPacketException;
import pinorobotics.rtpstalk.impl.spec.transport.io.exceptions.UnsupportedProtocolVersion;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class RtpsMessageReader {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsMessageReader.class);

    /** Returns empty when there is no RTPS message in the buffer or in case it is invalid. */
    public Optional<RtpsMessage> readRtpsMessage(ByteBuffer buf) throws Exception {
        var in = new RtpsInputKineticStream(buf);
        var ksr =
                new KineticStreamReader(in).withController(new RtpsKineticStreamReaderController());
        in.setKineticStreamReader(ksr);
        try {
            return Optional.of(ksr.read(RtpsMessage.class));
        } catch (NotRtpsPacketException e) {
            LOGGER.fine("Not RTPS packet, ignoring...");
            return Optional.empty();
        } catch (UnsupportedProtocolVersion e) {
            LOGGER.fine("RTPS protocol version {0} not supported", e.getProtocolVersion());
            return Optional.empty();
        }
    }
}
