package pinorobotics.rtpstalk;

import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.discovery.sedp.SedpService;
import pinorobotics.rtpstalk.discovery.spdp.SpdpService;
import pinorobotics.rtpstalk.transport.DataChannelFactory;

public class Test {

    public static void main(String[] args) throws Exception {
        XLogger.load("rtpstalk-debug.properties");
        var config = RtpsTalkConfiguration.DEFAULT;
        var channelFactory = new DataChannelFactory(config);
        var spdp = new SpdpService(config, channelFactory);
        new SedpService(config, channelFactory).start(spdp.getReader().getCache());
        spdp.start();
        System.in.read();
    }
}
