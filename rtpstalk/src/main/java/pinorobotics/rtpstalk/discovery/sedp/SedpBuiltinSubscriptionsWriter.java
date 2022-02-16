package pinorobotics.rtpstalk.discovery.sedp;

import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.writer.RtpsWriter;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;

public class SedpBuiltinSubscriptionsWriter extends RtpsWriter<ParameterList> {

    public SedpBuiltinSubscriptionsWriter(RtpsTalkConfiguration config) {
        super(new Guid(config.getGuidPrefix(),
                EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER.getValue()),
                EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR.getValue());
    }

}
