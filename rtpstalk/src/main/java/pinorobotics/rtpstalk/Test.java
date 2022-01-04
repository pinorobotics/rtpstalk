package pinorobotics.rtpstalk;

import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.spdp.SpdpBuiltinParticipantReader;

public class Test {

	public static void main(String[] args) throws Exception {
		XLogger.load("rtpstalk-debug.properties");
		new SpdpBuiltinParticipantReader().start();
		System.in.read();
	}
}
