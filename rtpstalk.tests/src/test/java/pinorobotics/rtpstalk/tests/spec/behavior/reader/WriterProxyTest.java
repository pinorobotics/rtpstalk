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

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.WriterProxy;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class WriterProxyTest {

    @Test
    public void test_equals() {
        var wp =
                new WriterProxy(
                        TestConstants.TEST_TRACING_TOKEN,
                        TestConstants.TEST_CONFIG,
                        new Guid(
                                TestConstants.TEST_GUID_PREFIX,
                                EntityId.Predefined.ENTITYID_PARTICIPANT.getValue()),
                        new Guid(
                                TestConstants.TEST_REMOTE_GUID_PREFIX,
                                EntityId.Predefined.ENTITYID_PARTICIPANT.getValue()),
                        List.of());
        IntStream.range(0, 15).forEach(wp::missingChangesUpdate);
        wp.lostChangesUpdate(10);
        Assertions.assertArrayEquals(new long[] {10, 11, 12, 13, 14}, wp.missingChanges());
    }
}
