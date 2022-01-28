package pinorobotics.rtpstalk.transport.io;

import java.nio.ByteBuffer;
import java.util.Optional;

import id.kineticstreamer.KineticStreamReader;
import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.transport.io.exceptions.NotRtpsPacketException;
import pinorobotics.rtpstalk.transport.io.exceptions.UnsupportedProtocolVersion;

public class RtpsMessageReader {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsMessageReader.class);

    /**
     * Returns empty when there is no RTPS message in the buffer or in case it is
     * invalid.
     */
    public Optional<RtpsMessage> readRtpsMessage(ByteBuffer buf) throws Exception {
        var in = new RtpsInputKineticStream(buf);
        var ksr = new KineticStreamReader(in)
                .withController(new RtpsKineticStreamReaderController());
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
