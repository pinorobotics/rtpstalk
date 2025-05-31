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
package pinorobotics.rtpstalk.tests.messages;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pinorobotics.rtpstalk.impl.messages.RtpsMessageAggregator;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsMessageAggregatorTest {

    @ParameterizedTest
    @CsvSource({
        "20, 0",
        "24, 0",
        "35, 0",
        // headerLen + submessageHeaderLen + submessageLen
        "36, 1",
        "51, 1",
        // headerLen + submessageHeaderLen + submessageLen + submessageHeaderLen + submessageLen
        "52, 2",
        "68, 3"
    })
    public void test(int maxSubmessagesTotalSize, int expectedMessagesAddedCount) {
        var headerLen = LengthCalculator.getInstance().getHeaderLength();
        Assertions.assertEquals(20, headerLen);

        var submessageHeaderLen = LengthCalculator.getInstance().getSubmessageHeaderLength();
        Assertions.assertEquals(4, submessageHeaderLen);

        var submessage = new InfoDestination(TestConstants.TEST_REMOTE_GUID_PREFIX);
        var submessageLen = submessage.submessageHeader.submessageLength.getUnsigned();
        Assertions.assertEquals(12, submessageLen);

        var agg =
                new RtpsMessageAggregator(
                        TestConstants.TEST_TRACING_TOKEN,
                        TestConstants.TEST_GUID_PREFIX,
                        maxSubmessagesTotalSize);
        var actualMessagesAddedCount = 0;
        while (agg.add(submessage)) actualMessagesAddedCount++;
        Assertions.assertEquals(expectedMessagesAddedCount, actualMessagesAddedCount);
    }
}
