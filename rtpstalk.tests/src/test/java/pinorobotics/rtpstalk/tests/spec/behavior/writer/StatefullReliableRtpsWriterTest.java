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
package pinorobotics.rtpstalk.tests.spec.behavior.writer;

import id.xfunction.concurrent.SameThreadExecutorService;
import id.xfunction.lang.XThread;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.SubmissionPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.AckNack;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannelFactory;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class StatefullReliableRtpsWriterTest {

    @Test
    public void test_history_cleanup() throws IOException {
        int millis = 100;
        var config =
                TestConstants.TEST_CONFIG_BUILDER
                        .historyCacheMaxSize(7)
                        .heartbeatPeriod(Duration.ofMillis(millis))
                        .build();
        var count = 0;
        var writerGuid =
                new Guid(
                        TestConstants.TEST_GUID_PREFIX, new EntityId(22, EntityKind.WRITER_NO_KEY));
        var readerGuid =
                new Guid(
                        TestConstants.TEST_GUID_PREFIX, new EntityId(22, EntityKind.READER_NO_KEY));
        var operatingEntities = new OperatingEntities(TestConstants.TEST_TRACING_TOKEN);
        try (var writer =
                        new StatefullReliableRtpsWriter<>(
                                config,
                                new TestDataChannelFactory(config),
                                operatingEntities,
                                TestConstants.TEST_TRACING_TOKEN,
                                writerGuid.entityId);
                var reader =
                        new StatefullReliableRtpsReader<>(
                                config,
                                TestConstants.TEST_TRACING_TOKEN,
                                operatingEntities,
                                readerGuid.entityId);
                var publisher =
                        new SubmissionPublisher<RtpsTalkDataMessage>(
                                new SameThreadExecutorService(), 1)) {
            publisher.subscribe(writer);
            publisher.submit(new RtpsTalkDataMessage("hello"));
            count++;
            Assertions.assertEquals(count, writer.getWriterCache().getNumberOfChanges(writerGuid));
            writer.matchedReaderAdd(
                    readerGuid, List.of(TestConstants.TEST_REMOTE_DEFAULT_UNICAST_LOCATOR));
            for (int i = 0; i < 5; i++) {
                publisher.submit(new RtpsTalkDataMessage("hello"));
                count++;
            }
            var ackNack =
                    new AckNack(
                            readerGuid.entityId,
                            writerGuid.entityId,
                            new SequenceNumberSet(count, 0),
                            new Count(count));
            reader.onAckNack(writerGuid.guidPrefix, ackNack);

            for (int i = 0; i < 5; i++) {
                publisher.submit(new RtpsTalkDataMessage("hello"));
                count++;
            }
            ackNack.readerSNState = new SequenceNumberSet(count, 0);
            reader.onAckNack(writerGuid.guidPrefix, ackNack);

            while (writer.getWriterCache().getNumberOfChanges(writerGuid) > 1)
                XThread.sleep(millis);
        }
    }
}
