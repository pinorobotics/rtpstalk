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
package pinorobotics.rtpstalk.tests.behavior.writer;

import id.xfunctiontests.XAsserts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.behavior.writer.RtpsDataMessageBuilder;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsDataMessageBuilderTest {

    @Test
    public void test_multiple_messages() {
        var builder =
                new RtpsDataMessageBuilder(
                        new RtpsTalkConfigurationInternal(
                                new RtpsTalkConfiguration.Builder()
                                        .packetBufferSize(10_000)
                                        .build()),
                        TestConstants.TEST_TRACING_TOKEN,
                        TestConstants.TEST_GUID_PREFIX);
        for (int i = 1; i <= 10; i++) {
            builder.add(i, new RtpsTalkDataMessage(new byte[1300]));
        }
        var actual = builder.build(new EntityId(), new EntityId());
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(8, actual.get(0).submessages.length);
        Assertions.assertEquals(4, actual.get(1).submessages.length);
        Assertions.assertEquals(InfoTimestamp.class, actual.get(0).submessages[0].getClass());
        Assertions.assertEquals(InfoTimestamp.class, actual.get(1).submessages[0].getClass());
        Assertions.assertEquals(Data.class, actual.get(1).submessages[1].getClass());
    }

    @Test
    public void test_infodst() {
        var builder =
                new RtpsDataMessageBuilder(
                        new RtpsTalkConfigurationInternal(
                                new RtpsTalkConfiguration.Builder()
                                        .packetBufferSize(10_000)
                                        .build()),
                        TestConstants.TEST_TRACING_TOKEN,
                        TestConstants.TEST_GUID_PREFIX,
                        new GuidPrefix("aaaaaaaaaaaaaaaaaaaaaaaa"));
        builder.add(1, new RtpsTalkDataMessage(new byte[13]));
        var actual = builder.build(new EntityId(), new EntityId());
        XAsserts.assertMatches(getClass(), "test_infodst", actual.toString());
    }
}
