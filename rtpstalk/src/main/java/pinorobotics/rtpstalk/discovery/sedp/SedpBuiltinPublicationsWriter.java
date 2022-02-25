package pinorobotics.rtpstalk.discovery.sedp;

import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.writer.StatefullRtpsWriter;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.transport.DataChannelFactory;

public class SedpBuiltinPublicationsWriter extends StatefullRtpsWriter<ParameterList> {

    public SedpBuiltinPublicationsWriter(DataChannelFactory channelFactory, RtpsTalkConfiguration config) {
        super(channelFactory, new Guid(config.getGuidPrefix(),
                EntityId.Predefined.ENTITYID_SEDP_BUILTIN_PUBLICATIONS_ANNOUNCER.getValue()),
                EntityId.Predefined.ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR.getValue(),
                config.getHeartbeatPeriod());
    }
}
