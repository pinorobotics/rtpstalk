package pinorobotics.rtpstalk.discovery.sedp;

import java.nio.channels.DatagramChannel;
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;

public class SedpBuiltinPublicationsReader extends StatefullRtpsReader {

    public SedpBuiltinPublicationsReader(DatagramChannel dc, int packetBufferSize) {
        super(dc, packetBufferSize);
    }

}
