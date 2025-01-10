/*
 * Copyright 2025 pinorobotics
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
package pinorobotics.rtpstalk.tests.spec.userdata;

import id.xfunction.XByte;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.userdata.SampleIdentityProcessor;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.messages.UserParameterId;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class SampleIdentityProcessorTest {

    @Test
    void test() throws Exception {
        var proc = new SampleIdentityProcessor();
        var guidPrefixA = new GuidPrefix("010f43477e1887aa00000000");
        var guidPrefixB = new GuidPrefix("010f43477e1887bb00000000");
        var sampleIdentityA = XByte.fromHex("010f43477e1887aa00000000000012040000000001000000");
        var sampleIdentityNoMatch =
                XByte.fromHex("010f4347f417bcd800000000000012040000000001000000");

        // test for match
        Assertions.assertEquals(
                false,
                proc.shouldReplaceWithGap(
                        new RtpsTalkDataMessage(
                                new Parameters(
                                        Map.of(
                                                UserParameterId.PID_FASTDDS_SAMPLE_IDENTITY,
                                                sampleIdentityA))),
                        guidPrefixA,
                        guidPrefixB));

        // test for no match
        Assertions.assertEquals(
                true,
                proc.shouldReplaceWithGap(
                        new RtpsTalkDataMessage(
                                new Parameters(
                                        Map.of(
                                                UserParameterId.PID_FASTDDS_SAMPLE_IDENTITY,
                                                sampleIdentityNoMatch))),
                        guidPrefixA,
                        guidPrefixB));

        // test for non sample identity value
        Assertions.assertEquals(
                false,
                proc.shouldReplaceWithGap(
                        new RtpsTalkDataMessage(
                                new Parameters(
                                        Map.of(
                                                UserParameterId.PID_FASTDDS_SAMPLE_IDENTITY,
                                                "aaaaaa".getBytes()))),
                        guidPrefixA,
                        guidPrefixB));
    }
}
