package pinorobotics.rtpstalk.userdata;

import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;

public class DataReader extends StatefullRtpsReader {

    public DataReader(RtpsTalkConfiguration config, EntityId entityId) {
        super(config, entityId);
    }

}
