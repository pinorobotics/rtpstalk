package pinorobotics.rtpstalk.discovery.sedp;

import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;

public class SedpBuiltinPublicationsReader extends StatefullRtpsReader {

    public SedpBuiltinPublicationsReader(RtpsTalkConfiguration config) {
        super(config, EntityId.Predefined.ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR.getValue());
    }

}
