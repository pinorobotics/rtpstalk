package pinorobotics.rtpstalk.userdata;

import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.writer.StatefullRtpsWriter;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.transport.DataChannelFactory;

public class DataWriter extends StatefullRtpsWriter<RawData> {

    public DataWriter(RtpsTalkConfiguration config, DataChannelFactory channelFactory,
            EntityId writerEntityId, EntityId readerEntityId) {
        super(channelFactory, new Guid(config.getGuidPrefix(),
                writerEntityId),
                readerEntityId,
                config.getHeartbeatPeriod());
    }

}
