package pinorobotics.rtpstalk;

import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.discovery.sedp.SedpService;
import pinorobotics.rtpstalk.discovery.spdp.SpdpService;

public class Test {

    public static void main(String[] args) throws Exception {
        XLogger.load("rtpstalk-debug.properties");
        new SedpService().start();
        new SpdpService().start();
        System.in.read();
    }
}
