package pinorobotics.rtpstalk.behavior.liveliness;

import id.xfunction.XAsserts;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.messages.BuiltinEndpointQos.EndpointQos;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;

/**
 * Reliable liveliness reader
 */
public class BuiltinParticipantMessageReader extends StatefullRtpsReader {

    public BuiltinParticipantMessageReader(RtpsTalkConfiguration config) {
        super(config, EntityId.Predefined.ENTITYID_P2P_BUILTIN_PARTICIPANT_MESSAGE_READER.getValue());
        XAsserts.assertTrue(config.getBuiltinEndpointQos() != EndpointQos.BEST_EFFORT_PARTICIPANT_MESSAGE_DATA_READER,
                "Not supported with best effort builtin endpoint Qos");
    }

}
