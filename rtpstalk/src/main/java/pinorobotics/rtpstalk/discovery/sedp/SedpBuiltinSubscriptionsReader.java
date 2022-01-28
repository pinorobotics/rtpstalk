package pinorobotics.rtpstalk.discovery.sedp;

import java.nio.channels.DatagramChannel;
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;

public class SedpBuiltinSubscriptionsReader extends StatefullRtpsReader {

    public SedpBuiltinSubscriptionsReader(GuidPrefix guidPrefix, DatagramChannel dc, int packetBufferSize) {
        super(new Guid(guidPrefix, EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR.getValue()), dc,
                packetBufferSize);
    }

}
