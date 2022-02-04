package pinorobotics.rtpstalk.transport.io;

import id.kineticstreamer.KineticStreamWriterController;
import id.kineticstreamer.OutputKineticStream;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumberSet;

class RtpsKineticStreamWriterController extends KineticStreamWriterController {

    @Override
    public Result onNextObject(OutputKineticStream in, Object obj) throws Exception {
        var rtpsStream = (RtpsOutputKineticStream) in;
        if (obj instanceof ParameterList pl) {
            // writing it manually since we convert it to custom type
            rtpsStream.writeParameterList(pl);
            return new Result(true);
        }
        if (obj instanceof SequenceNumber num) {
            // writing it manually in custom format
            rtpsStream.writeSequenceNumber(num);
            return new Result(true);
        }
        if (obj instanceof SequenceNumberSet set) {
            // writing it manually in custom format
            rtpsStream.writeSequenceNumberSet(set);
            return new Result(true);
        }
        return Result.CONTINUE;
    }

}