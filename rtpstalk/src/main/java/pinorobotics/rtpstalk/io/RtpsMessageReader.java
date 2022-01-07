package pinorobotics.rtpstalk.io;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import id.kineticstreamer.KineticStreamReader;
import id.xfunction.XAsserts;
import id.xfunction.lang.XRuntimeException;
import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.entities.Data;
import pinorobotics.rtpstalk.entities.Header;
import pinorobotics.rtpstalk.entities.InfoTimestamp;
import pinorobotics.rtpstalk.entities.ProtocolId;
import pinorobotics.rtpstalk.entities.ProtocolVersion;
import pinorobotics.rtpstalk.entities.RtpsMessage;
import pinorobotics.rtpstalk.entities.SerializedPayload;
import pinorobotics.rtpstalk.entities.SerializedPayloadHeader;
import pinorobotics.rtpstalk.entities.Submessage;
import pinorobotics.rtpstalk.entities.SubmessageHeader;

public class RtpsMessageReader {

	private static final XLogger LOGGER = XLogger.getLogger(RtpsMessageReader.class);
	
	/**
	 * Returns empty when there is no RTPS message in the buffer
	 */
	public Optional<RtpsMessage> readRtpsMessage(ByteBuffer buf) throws Exception {
	     var in = new RtpcInputKineticStream(buf);
	     var ksr = new KineticStreamReader(in)
	    		 .withController(new RtpcKineticStreamReaderController(in));
	     var header = ksr.read(Header.class);
	     if (!Objects.equals(header.protocolId, ProtocolId.Predefined.RTPS.getValue())) {
	    	 LOGGER.fine("Not RTPS packet, ignoring...");
	    	 return Optional.empty();
	     }
	     if (!Objects.equals(header.protocolVersion, ProtocolVersion.Predefined.Version_2_3.getValue())) {
	    	 throw new XRuntimeException("RTPS protocol version %s not supported", header.protocolVersion);
	     }
	     // TODO check little endian only
	     LOGGER.fine("header: {0}", header);
	     var submessages = new ArrayList<Submessage>();
	     while (buf.hasRemaining()) {
		     var submessageHeader = ksr.read(SubmessageHeader.class);
		     LOGGER.fine("submessageHeader: {0}", submessageHeader);
		     var submessageStart = buf.position();
		     LOGGER.fine("submessageStart: {0}", submessageStart);
		     var submessage = ksr.read(submessageHeader.submessageKind.getSubmessageClass());
		     submessage.submessageHeader = submessageHeader;
		     switch (submessage) {
		     case InfoTimestamp m: {
		    	 LOGGER.fine("submessageElement: {0}", m);
		    	 break;
		     }
		     case Data m: {
		    	 LOGGER.fine("submessageElement: {0}", m);
		    	 if (m.isInlineQos()) throw new UnsupportedOperationException();
		    	 buf.position(buf.position() + m.getBytesToSkip());
		    	 var payloadHeader = ksr.read(SerializedPayloadHeader.class);
		    	 LOGGER.fine("payloadHeader: {0}", payloadHeader);
		    	 // if PL_CDR_LE
		    	 var payload = in.readParameterList();
		    	 LOGGER.fine("payload: {0}", payload);
		    	 m.serializedPayload = new SerializedPayload(payloadHeader, payload);
		    	 break;
		     }
		     default: {
		    	 LOGGER.warning("Submessage kind {} is not supported", submessageHeader.submessageKind);
		     }
		     }
		     var submessageEnd = buf.position();
		     LOGGER.fine("submessageEnd: {0}", submessageEnd);
		     submessages.add(submessage);
		     XAsserts.assertEquals(submessageHeader.submessageLength, submessageEnd - submessageStart,
		    		 "Read message size does not match expected");
	     }
	     var message = new RtpsMessage(header, submessages);
	     return Optional.of(message);
	}
}
