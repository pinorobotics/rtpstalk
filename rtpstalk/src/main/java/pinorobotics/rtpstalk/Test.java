package pinorobotics.rtpstalk;

import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.discovery.sedp.SedpService;
import pinorobotics.rtpstalk.discovery.spdp.SpdpService;

public class Test {

    public static void main(String[] args) throws Exception {
        XLogger.load("rtpstalk-debug.properties");
        var config = RtpsTalkConfiguration.DEFAULT;
        var spdp = new SpdpService(config);
        spdp.start();
        new SedpService(config).start(spdp.getReader().getCache());
        System.in.read();
    }
}
