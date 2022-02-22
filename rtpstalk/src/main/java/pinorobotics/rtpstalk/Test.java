package pinorobotics.rtpstalk;

import id.xfunction.concurrent.flow.XSubscriber;
import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityKind;

public class Test {

    public static void main(String[] args) throws Exception {
        XLogger.load("rtpstalk-debug.properties");
        var printer = new XSubscriber<RawData>() {
            @Override
            public void onNext(RawData data) {
                System.out.println(data);
                subscription.request(1);
            }
        };
        new RtpsTalkClient().subscribe("rt/chatter", "std_msgs::msg::dds_::String_",
                new EntityId(new byte[] { 00, 00, 0x12 }, EntityKind.READER_NO_KEY), printer);
        System.in.read();
    }

}
