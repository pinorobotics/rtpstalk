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
package pinorobotics.rtpstalk.tests.spec.behavior.reader;

import id.xfunction.concurrent.SameThreadExecutorService;
import id.xfunction.concurrent.flow.CollectorSubscriber;
import id.xfunctiontests.XAsserts;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SubmissionPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class StatefullReliableRtpsReaderTest {

    @Test
    public void test_messages_from_non_matched_writers_are_ignored() {
        var reader =
                new StatefullReliableRtpsReader<>(
                        TestConstants.TEST_CONFIG,
                        TestConstants.TEST_TRACING_TOKEN,
                        RtpsTalkDataMessage.class,
                        new SameThreadExecutorService(),
                        new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN),
                        TestConstants.TEST_READER_ENTITY_ID);
        var items = new ArrayList<RtpsTalkDataMessage>();
        reader.subscribe(new CollectorSubscriber<>(items));
        try (var publisher =
                new SubmissionPublisher<RtpsMessage>(new SameThreadExecutorService(), 1)) {
            publisher.subscribe(reader);

            var messages =
                    List.of(
                            RtpsReaderTest.newRtpsMessage(1, "aaaaa"),
                            RtpsReaderTest.newRtpsMessage(2, "bbbb"),
                            RtpsReaderTest.newRtpsMessage(5, "e"),
                            RtpsReaderTest.newRtpsMessage(4, "dd"),
                            RtpsReaderTest.newRtpsMessage(3, "ccc"),
                            RtpsReaderTest.newRtpsMessage(6, "ffff"));
            messages.forEach(publisher::submit);
            Assertions.assertEquals("[]", items.toString());

            reader.matchedWriterAdd(
                    TestConstants.TEST_GUID_WRITER,
                    List.of(TestConstants.TEST_DEFAULT_UNICAST_LOCATOR));
            messages.forEach(publisher::submit);
            XAsserts.assertEquals(getClass(), "test_RtpsReader_reliable2", items.toString());
        }
    }
}
