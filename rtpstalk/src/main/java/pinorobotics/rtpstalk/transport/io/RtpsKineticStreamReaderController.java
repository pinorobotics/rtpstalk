package pinorobotics.rtpstalk.transport.io;

import id.kineticstreamer.InputKineticStream;
import id.kineticstreamer.KineticStreamReaderController;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumberSet;

class RtpsKineticStreamReaderController extends KineticStreamReaderController {

    @Override
    public Result onNextObject(InputKineticStream in, Object obj, Class<?> fieldType) throws Exception {
        var rtpsStream = (RtpsInputKineticStream) in;
        if (fieldType == Header.class) {
            // reading it manually to perform validation
            return new Result(true, rtpsStream.readHeader());
        }
        if (fieldType == SequenceNumber.class) {
            // reading it manually in custom format
            return new Result(true, rtpsStream.readSequenceNumber());
        }
        if (fieldType == SequenceNumberSet.class) {
            // reading it manually in custom format
            return new Result(true, rtpsStream.readSequenceNumberSet());
        }
        return Result.CONTINUE;
    }

}
