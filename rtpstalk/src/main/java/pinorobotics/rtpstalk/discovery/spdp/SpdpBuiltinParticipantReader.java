package pinorobotics.rtpstalk.discovery.spdp;

import java.nio.channels.DatagramChannel;
import pinorobotics.rtpstalk.behavior.reader.StatelessRtpsReader;

public class SpdpBuiltinParticipantReader extends StatelessRtpsReader {

    public SpdpBuiltinParticipantReader(DatagramChannel dc, int packetBufferSize) {
        super(dc, packetBufferSize);
    }

}
