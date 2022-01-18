package pinorobotics.rtpstalk.io;

import id.kineticstreamer.KineticStreamWriterController;
import id.kineticstreamer.OutputKineticStream;
import pinorobotics.rtpstalk.dto.submessages.elements.ParameterList;

public class RtpsKineticStreamWriterController extends KineticStreamWriterController {

    @Override
    public Result onNextObject(OutputKineticStream in, Object obj) throws Exception {
        var rtpsStream = (RtpsOutputKineticStream) in;
        if (obj instanceof ParameterList pl) {
            // writing it manually since we convert it to custom type
            rtpsStream.writeParameterList(pl);
            return new Result(true);
        }
        return Result.CONTINUE;
    }

}
