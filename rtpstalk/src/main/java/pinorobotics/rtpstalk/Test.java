/*
 * Copyright 2022 rtpstalk project
 * 
 * Website: https://github.com/pinorobotics/rtpstalk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pinorobotics.rtpstalk;

import id.xfunction.XByte;
import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.lang.XThread;
import id.xfunction.logging.XLogger;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityKind;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class Test {

    public static void main(String[] args) throws Exception {
        XLogger.load("rtpstalk-debug.properties");
        var printer =
                new SimpleSubscriber<byte[]>() {
                    @Override
                    public void onNext(byte[] data) {
                        System.out.println(data);
                        subscription.request(1);
                    }
                };
        //        new RtpsTalkClient().subscribe("rt/chatter", "std_msgs::msg::dds_::String_",
        //                new EntityId(new byte[] { 00, 00, 0x12 }, EntityKind.READER_NO_KEY),
        // printer);
        var publisher = new SubmissionPublisher<RawData>();
        new RtpsTalkClient()
                .publish(
                        "rt/chatter",
                        "std_msgs::msg::dds_::String_",
                        new EntityId(new byte[] {00, 00, 0x12}, EntityKind.WRITER_NO_KEY),
                        new EntityId(new byte[] {00, 00, 0x12}, EntityKind.READER_NO_KEY),
                        publisher);
        while (true) {
            publisher.submit(
                    new RawData(
                            XByte.castToByteArray(
                                    0x10, 0x00, 0x00, 0x00, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20,
                                    0x57, 0x6f, 0x72, 0x6c, 0x64, 0x3a, 0x20, 0x31, 0x36, 0x00)));
            XThread.sleep(1000);
        }
    }
}
