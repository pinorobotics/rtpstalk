package pinorobotics.rtpstalk;

import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.discovery.sedp.SedpService;
import pinorobotics.rtpstalk.discovery.spdp.SpdpService;

public class Test {

    public static void main(String[] args) throws Exception {
        XLogger.load("rtpstalk-debug.properties");
        var spdp = new SpdpService();
        spdp.start();
        new SedpService(spdp.getReader()).start();
        System.in.read();
    }
}
