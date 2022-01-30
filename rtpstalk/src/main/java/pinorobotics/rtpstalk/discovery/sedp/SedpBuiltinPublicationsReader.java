package pinorobotics.rtpstalk.discovery.sedp;

import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;

public class SedpBuiltinPublicationsReader extends StatefullRtpsReader {

    public SedpBuiltinPublicationsReader(GuidPrefix guidPrefix) {
        super(new Guid(guidPrefix, EntityId.Predefined.ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR.getValue()));
    }

}
