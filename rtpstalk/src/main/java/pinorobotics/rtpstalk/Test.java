package pinorobotics.rtpstalk;

import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.spdp.SPDPbuiltinParticipantReader;

public class Test {

	public static void main(String[] args) throws Exception {
		XLogger.load("rtpstalk-debug.properties");
		new SPDPbuiltinParticipantReader().start();
		System.in.read();
	}
}
