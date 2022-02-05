package pinorobotics.rtpstalk.discovery.sedp;

import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;

public class SedpBuiltinSubscriptionsReader extends StatefullRtpsReader {

    public SedpBuiltinSubscriptionsReader(RtpsTalkConfiguration config) {
        super(config, EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR.getValue());
    }

}
