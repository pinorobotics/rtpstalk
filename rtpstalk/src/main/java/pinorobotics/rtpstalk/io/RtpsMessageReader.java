package pinorobotics.rtpstalk.io;

import java.nio.ByteBuffer;
import java.util.Optional;

import id.kineticstreamer.KineticStreamReader;
import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.dto.RtpsMessage;
import pinorobotics.rtpstalk.io.exceptions.NotRtpsPacketException;
import pinorobotics.rtpstalk.io.exceptions.UnsupportedProtocolVersion;

public class RtpsMessageReader {

	private static final XLogger LOGGER = XLogger.getLogger(RtpsMessageReader.class);
	
	/**
	 * Returns empty when there is no RTPS message in the buffer
	 */
	public Optional<RtpsMessage> readRtpsMessage(ByteBuffer buf) throws Exception {
	     var in = new RtpcInputKineticStream(buf);
	     var ksr = new KineticStreamReader(in)
	    		 .withController(new RtpcKineticStreamReaderController(in));
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
