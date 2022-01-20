package pinorobotics.rtpstalk.io;

import id.kineticstreamer.InputKineticStream;
import id.kineticstreamer.KineticStreamReaderController;
import pinorobotics.rtpstalk.messages.Header;

public class RtpsKineticStreamReaderController extends KineticStreamReaderController {

    @Override
    public Result onNextObject(InputKineticStream in, Object obj, Class<?> fieldType) throws Exception {
        var rtpsStream = (RtpsInputKineticStream) in;
        if (fieldType == Header.class) {
            // reading it manually to perform validation
            return new Result(true, rtpsStream.readHeader());
        }
        return Result.CONTINUE;
    }

}
