package pinorobotics.rtpstalk.io;

import id.kineticstreamer.KineticStreamReaderController;
import pinorobotics.rtpstalk.dto.submessages.Locator;

public class RtpcKineticStreamReaderController extends KineticStreamReaderController {

	private RtpcInputKineticStream in;

	public RtpcKineticStreamReaderController(RtpcInputKineticStream in) {
		this.in = in;
	}

	@Override
	public Result onNextObject(Object obj, Class<?> fieldType) throws Exception {
		if (fieldType == Locator.class) {
			return new Result(true, in.readLocator());
		}
		return Result.CONTINUE;
	}

}
