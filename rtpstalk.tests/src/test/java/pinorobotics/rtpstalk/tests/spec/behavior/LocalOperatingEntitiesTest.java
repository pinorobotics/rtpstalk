/*
 * Copyright 2024 rtpstalk project
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

import static org.junit.jupiter.api.Assertions.assertThrows;

import id.xfunction.PreconditionException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.WriterSettings;
import pinorobotics.rtpstalk.impl.qos.WriterQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannelFactory;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class LocalOperatingEntitiesTest {
    @Test
    public void test() throws IOException {
        var operatingEntities = new LocalOperatingEntities(TestConstants.TEST_TRACING_TOKEN);
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
            assertThrows(
                    PreconditionException.class,
                    () -> operatingEntities.getLocalWriters().add(reliableWriter));
        }
    }
}
