package pinorobotics.rtpstalk.transport.io;

import java.nio.ByteBuffer;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import id.kineticstreamer.KineticStreamWriter;

public class RtpsMessageWriter {

    public void writeRtpsMessage(RtpsMessage data, ByteBuffer buf) throws Exception {
        var out = new RtpsOutputKineticStream(buf);
        var ksw = new KineticStreamWriter(out)
                .withController(new RtpsKineticStreamWriterController());
        out.setWriter(ksw);
        ksw.write(data);
    }

}
