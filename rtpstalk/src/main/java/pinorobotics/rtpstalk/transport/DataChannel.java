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
package pinorobotics.rtpstalk.transport;

import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.transport.io.RtpsMessageReader;
import pinorobotics.rtpstalk.transport.io.RtpsMessageWriter;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class DataChannel implements AutoCloseable {
    private static final XLogger LOGGER = XLogger.getLogger(DataChannel.class);

    private RtpsMessageReader reader = new RtpsMessageReader();
    private RtpsMessageWriter writer = new RtpsMessageWriter();
    private DatagramChannel dataChannel;
    private int packetBufferSize;
    private GuidPrefix guidPrefix;
    private SocketAddress target;

    protected DataChannel(
            DatagramChannel dataChannel,
            SocketAddress target,
            GuidPrefix guidPrefix,
            int packetBufferSize) {
        this.dataChannel = dataChannel;
        this.target = target;
        this.guidPrefix = guidPrefix;
        this.packetBufferSize = packetBufferSize;
    }

    /** @throws AsynchronousCloseException if channel was closed during read */
    public RtpsMessage receive() throws Exception {
        while (true) {
            var buf = ByteBuffer.allocate(packetBufferSize);
            dataChannel.receive(buf);
            var len = buf.position();
            buf.rewind();
            buf.limit(len);
            LOGGER.fine("Received UDP packet of size {0}", len);
            var messageOpt = reader.readRtpsMessage(buf);
            if (messageOpt.isEmpty()) continue;
            var message = messageOpt.get();
            if (message.header.guidPrefix.equals(guidPrefix)) {
                LOGGER.fine("Received its own message, ignoring...");
                continue;
            }
            return message;
        }
    }

    public void send(RtpsMessage message) {
        var buf = ByteBuffer.allocate(packetBufferSize);
        buf.rewind();
        buf.limit(buf.capacity());
        try {
            writer.writeRtpsMessage(message, buf);
            buf.limit(buf.position());
            buf.rewind();
            dataChannel.send(buf, target);
        } catch (Throwable e) {
            LOGGER.severe(e);
            return;
        }
    }

    @Override
    public void close() {
        try {
            dataChannel.close();
        } catch (IOException e) {
            LOGGER.severe(e);
        }
        LOGGER.fine("Closed");
    }
}
