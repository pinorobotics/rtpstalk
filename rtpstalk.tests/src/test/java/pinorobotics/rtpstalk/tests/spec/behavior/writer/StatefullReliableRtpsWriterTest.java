/*
 * Copyright 2022 pinorobotics
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

import id.xfunction.concurrent.flow.SynchronousPublisher;
import id.xfunction.lang.XThread;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.WriterSettings;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.qos.WriterQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Header;
import pinorobotics.rtpstalk.impl.spec.messages.ProtocolId;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy.Kind;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.AckNack;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;
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
                new RtpsTalkConfigurationInternal(
                        TestConstants.TEST_CONFIG_BUILDER
                                .historyCacheMaxSize(7)
                                .heartbeatPeriod(Duration.ofMillis(millis))
                                .build());
        var count = 0;
        var writerGuid =
                new Guid(
                        TestConstants.TEST_GUID_PREFIX, new EntityId(22, EntityKind.WRITER_NO_KEY));
        var readerGuid =
                new Guid(
                        TestConstants.TEST_GUID_PREFIX, new EntityId(22, EntityKind.READER_NO_KEY));
        var operatingEntities = new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN);
        try (var writer =
                        new StatefullReliableRtpsWriter<>(
                                config,
                                TestConstants.TEST_TRACING_TOKEN,
                                TestConstants.TEST_PUBLISHER_EXECUTOR,
                                new TestDataChannelFactory(config.publicConfig()),
                                operatingEntities,
                                writerGuid.entityId,
                                new WriterQosPolicySet(
                                        ReliabilityQosPolicy.Kind.RELIABLE,
                                        DurabilityQosPolicy.Kind.VOLATILE_DURABILITY_QOS),
                                new WriterSettings(false));
                var publisher = new SynchronousPublisher<RtpsTalkDataMessage>();
                var receiver = new SynchronousPublisher<RtpsMessage>(); ) {
            receiver.subscribe(writer.getWriterReader());
            publisher.subscribe(writer);
            publisher.submit(new RtpsTalkDataMessage("hello"));
            count++;
            Assertions.assertEquals(count, writer.getWriterCache().getNumberOfChanges(writerGuid));
            writer.matchedReaderAdd(
                    readerGuid,
                    List.of(TestConstants.TEST_REMOTE_DEFAULT_UNICAST_LOCATOR),
                    new ReaderQosPolicySet());
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
            var message =
                    new RtpsMessage(
                            new Header(
                                    ProtocolId.Predefined.RTPS.getValue(),
                                    ProtocolVersion.Predefined.Version_2_3.getValue(),
                                    VendorId.Predefined.FASTRTPS.getValue(),
                                    writerGuid.guidPrefix),
                            ackNack);
            receiver.submit(message);

            for (int i = 0; i < 5; i++) {
                publisher.submit(new RtpsTalkDataMessage("hello"));
                count++;
            }
            // ack all except last one
            ackNack.readerSNState = new SequenceNumberSet(count, 0);
            receiver.submit(message);

            while (writer.getWriterCache().getNumberOfChanges(writerGuid) > 1)
                XThread.sleep(millis);

            // there is still last change with seqNum == count, so we remove the reader
            // this will test that writer would be able to discard messages and close
            writer.matchedReaderRemove(readerGuid);
        }
    }

    @Test
    public void test_ignore_pendings_changes_for_best_effort_readers() throws IOException {
        var writerGuid =
                new Guid(
                        TestConstants.TEST_GUID_PREFIX, new EntityId(22, EntityKind.WRITER_NO_KEY));
        var readerGuid =
                new Guid(
                        TestConstants.TEST_GUID_PREFIX, new EntityId(22, EntityKind.READER_NO_KEY));
        var operatingEntities = new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN);
        try (var reliableWriter =
                        new StatefullReliableRtpsWriter<>(
                                TestConstants.TEST_CONFIG_INTERNAL,
                                TestConstants.TEST_TRACING_TOKEN,
                                TestConstants.TEST_PUBLISHER_EXECUTOR,
                                new TestDataChannelFactory(),
                                operatingEntities,
                                writerGuid.entityId,
                                new WriterQosPolicySet(
                                        ReliabilityQosPolicy.Kind.RELIABLE,
                                        DurabilityQosPolicy.Kind.VOLATILE_DURABILITY_QOS),
                                new WriterSettings(false));
                var publisher = new SynchronousPublisher<RtpsTalkDataMessage>(); ) {
            publisher.subscribe(reliableWriter);
            reliableWriter.matchedReaderAdd(
                    readerGuid,
                    List.of(TestConstants.TEST_REMOTE_DEFAULT_UNICAST_LOCATOR),
                    new ReaderQosPolicySet(
                            Kind.BEST_EFFORT,
                            DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS));
            publisher.submit(new RtpsTalkDataMessage("hello"));
        }
        // writer is closed and pending changes discarded
    }
}
