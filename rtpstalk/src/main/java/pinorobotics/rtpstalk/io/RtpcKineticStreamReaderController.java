package pinorobotics.rtpstalk.io;

import id.kineticstreamer.InputKineticStream;
import id.kineticstreamer.KineticStreamReaderController;
import pinorobotics.rtpstalk.dto.submessages.Header;

public class RtpcKineticStreamReaderController extends KineticStreamReaderController {

	@Override
	public Result onNextObject(InputKineticStream in, Object obj, Class<?> fieldType) throws Exception {
		var rtpsStream = (RtpcInputKineticStream)in;
		if (fieldType == Header.class) {
			// reading it manually to perform validation
			return new Result(true, rtpsStream.readHeader());
		}
		return Result.CONTINUE;
	}

}
