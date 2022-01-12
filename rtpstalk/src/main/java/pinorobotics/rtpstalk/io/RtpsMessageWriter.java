package pinorobotics.rtpstalk.io;

import java.nio.ByteBuffer;

import id.kineticstreamer.KineticStreamWriter;
import pinorobotics.rtpstalk.dto.RtpsMessage;

public class RtpsMessageWriter {

	public void writeRtpsMessage(RtpsMessage data, ByteBuffer buf) throws Exception {
		var out = new RtpcOutputKineticStream(buf);
		var ksw = new KineticStreamWriter(out)
				.withController(new RtpcKineticStreamWriterController());
		out.setWriter(ksw);
		ksw.write(data);
	}

}
