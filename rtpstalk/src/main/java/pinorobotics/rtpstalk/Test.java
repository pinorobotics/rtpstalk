package pinorobotics.rtpstalk;

import id.xfunction.XByte;
import id.xfunction.concurrent.flow.XSubscriber;
import id.xfunction.lang.XThread;
import id.xfunction.logging.XLogger;
import java.util.concurrent.SubmissionPublisher;
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
//        new RtpsTalkClient().subscribe("rt/chatter", "std_msgs::msg::dds_::String_",
//                new EntityId(new byte[] { 00, 00, 0x12 }, EntityKind.READER_NO_KEY), printer);
        var publisher = new SubmissionPublisher<RawData>();
        new RtpsTalkClient().publish("rt/chatter", "std_msgs::msg::dds_::String_",
                new EntityId(new byte[] { 00, 00, 0x12 }, EntityKind.WRITER_NO_KEY),
                new EntityId(new byte[] { 00, 00, 0x12 }, EntityKind.READER_NO_KEY), publisher);
        while (true) {
            publisher.submit(
                    new RawData(XByte.castToByteArray(0x10, 0x00, 0x00, 0x00, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20,
                            0x57, 0x6f, 0x72, 0x6c, 0x64, 0x3a, 0x20, 0x31, 0x36, 0x00)));
            XThread.sleep(1000);
        }
    }

}
