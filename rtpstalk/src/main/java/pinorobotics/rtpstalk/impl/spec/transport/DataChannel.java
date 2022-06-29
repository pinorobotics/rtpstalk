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
package pinorobotics.rtpstalk.impl.spec.transport;

import id.xfunction.Preconditions;
import id.xfunction.function.Unchecked;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageReader;
import pinorobotics.rtpstalk.impl.spec.transport.io.RtpsMessageWriter;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataChannel implements AutoCloseable {

    private RtpsMessageReader reader = new RtpsMessageReader();
    private RtpsMessageWriter writer = new RtpsMessageWriter();
    private DatagramChannel datagramChannel;
    private int packetBufferSize;
    private GuidPrefix guidPrefix;
    private SocketAddress target;
    private XLogger logger;

    protected DataChannel(
            TracingToken tracingToken,
            DatagramChannel datagramChannel,
            SocketAddress target,
            byte[] guidPrefix,
            int packetBufferSize) {
        this.datagramChannel = datagramChannel;
        this.target = target;
        this.guidPrefix = new GuidPrefix(guidPrefix);
        this.packetBufferSize = packetBufferSize;
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
    }

    /**
     * @throws AsynchronousCloseException if channel was closed during read
     */
    public RtpsMessage receive() throws Exception {
        while (true) {
            var buf = ByteBuffer.allocate(packetBufferSize);
            datagramChannel.receive(buf);
            var len = buf.position();
            buf.rewind();
            buf.limit(len);
            logger.fine("Received UDP packet of size {0}", len);
            var messageOpt = reader.readRtpsMessage(buf);
            if (messageOpt.isEmpty()) continue;
            var message = messageOpt.get();
            if (message.header.guidPrefix.equals(guidPrefix)) {
                logger.fine("Received its own message, ignoring...");
                continue;
            }
            return message;
        }
    }

    public void send(Guid remoteReader, RtpsMessage message) {
        logger.fine("Outgoing RTPS message for remote reader {0}: {1}", remoteReader, message);
        var buf = ByteBuffer.allocate(packetBufferSize);
        buf.rewind();
        buf.limit(buf.capacity());
        try {
            writer.writeRtpsMessage(message, buf);
            buf.limit(buf.position());
            buf.rewind();
            datagramChannel.send(buf, target);
        } catch (Throwable e) {
            logger.severe(e);
            return;
        }
    }

    @Override
    public void close() {
        try {
            datagramChannel.close();
        } catch (IOException e) {
            logger.severe(e);
        }
        logger.fine("Closed");
    }

    public int getLocalPort() {
        Preconditions.isTrue(
                Unchecked.getBoolean(
                        () -> datagramChannel.getLocalAddress() instanceof InetSocketAddress),
                "Inet socket required");
        try {
            var addr = (InetSocketAddress) datagramChannel.getLocalAddress();
            return addr.getPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
