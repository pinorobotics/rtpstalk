package pinorobotics.rtpstalk.discovery.spdp;

import java.nio.channels.DatagramChannel;
import pinorobotics.rtpstalk.behavior.reader.RtpsReader;

public class SpdpBuiltinParticipantReader extends RtpsReader {

    public SpdpBuiltinParticipantReader(DatagramChannel dc, int packetBufferSize) {
        super(dc, packetBufferSize);
    }

}
