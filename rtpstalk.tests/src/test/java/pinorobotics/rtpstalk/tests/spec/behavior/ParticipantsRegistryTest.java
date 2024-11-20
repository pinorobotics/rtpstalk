/*
 * Copyright 2024 pinorobotics
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
package pinorobotics.rtpstalk.tests.spec.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.WriterSettings;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.qos.WriterQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.ParticipantsRegistry;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannelFactory;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ParticipantsRegistryTest {

    @Test
    public void test() throws IOException {
        var operatingEntities = new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN);
        var registry =
                new ParticipantsRegistry(TestConstants.TEST_TRACING_TOKEN, operatingEntities);
        try (var reliableWriter =
                new StatefullReliableRtpsWriter<>(
                        TestConstants.TEST_CONFIG_INTERNAL,
                        TestConstants.TEST_TRACING_TOKEN,
                        TestConstants.TEST_PUBLISHER_EXECUTOR,
                        new TestDataChannelFactory(),
                        operatingEntities,
                        TestConstants.TEST_WRITER_ENTITY_ID,
                        new WriterQosPolicySet(
                                ReliabilityQosPolicy.Kind.RELIABLE,
                                DurabilityQosPolicy.Kind.VOLATILE_DURABILITY_QOS),
                        new WriterSettings(false))) {
            var matchedReader =
                    new Guid(
                            TestConstants.TEST_REMOTE_GUID_PREFIX,
                            TestConstants.TEST_READER_ENTITY_ID);
            reliableWriter.matchedReaderAdd(
                    matchedReader,
                    List.of(TestConstants.TEST_REMOTE_DEFAULT_UNICAST_LOCATOR),
                    new ReaderQosPolicySet());
            var matchedReaderParticipant =
                    new Guid(
                            TestConstants.TEST_REMOTE_GUID_PREFIX,
                            EntityId.Predefined.ENTITYID_PARTICIPANT);
            reliableWriter.matchedReaderAdd(
                    matchedReaderParticipant,
                    List.of(TestConstants.TEST_REMOTE_DEFAULT_UNICAST_LOCATOR),
                    new ReaderQosPolicySet());
            Supplier<Long> countMatchedReaders =
                    () ->
                            Stream.of(matchedReader, matchedReaderParticipant)
                                    .map(reliableWriter::matchedReaderLookup)
                                    .filter(Optional::isPresent)
                                    .count();
            assertEquals(2, countMatchedReaders.get());
            registry.removeParticipant(matchedReaderParticipant);
            assertEquals(0, countMatchedReaders.get());
        }
    }
}
