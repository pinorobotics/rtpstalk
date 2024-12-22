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
package pinorobotics.rtpstalk.tests.spec.behavior.writer;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.ReliableReaderProxy;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class ReliableReaderProxyTest {

    @Test
    public void test_history_cleanup() throws IOException {
        var readerProxy =
                new ReliableReaderProxy(
                        TestConstants.TEST_GUID_READER, List.of(), null, new ReaderQosPolicySet());
        Assertions.assertEquals("[]", readerProxy.requestedChanges().toString());
        Assertions.assertEquals(0, readerProxy.getHighestAckedSeqNum());
        readerProxy.ackedChanges(100);
        Assertions.assertEquals("[]", readerProxy.requestedChanges().toString());
        Assertions.assertEquals(100, readerProxy.getHighestAckedSeqNum());
        readerProxy.requestedChanges(List.of(1L));
        Assertions.assertEquals("[1]", readerProxy.requestedChanges().toString());
        Assertions.assertEquals(100, readerProxy.getHighestAckedSeqNum());
        readerProxy.requestedChanges(List.of(1L, 1L, 1L, 1L));
        Assertions.assertEquals("[1]", readerProxy.requestedChanges().toString());
        Assertions.assertEquals(100, readerProxy.getHighestAckedSeqNum());
        readerProxy.requestedChanges(List.of(2L, 2L));
        Assertions.assertEquals("[2]", readerProxy.requestedChanges().toString());
        Assertions.assertEquals(100, readerProxy.getHighestAckedSeqNum());
        readerProxy.requestedChanges(List.of(102L, 121L, 15L, 121L));
        Assertions.assertEquals("[15, 102, 121]", readerProxy.requestedChanges().toString());
        Assertions.assertEquals(100, readerProxy.getHighestAckedSeqNum());
        readerProxy.ackedChanges(110);
        Assertions.assertEquals("[121]", readerProxy.requestedChanges().toString());
        Assertions.assertEquals(110, readerProxy.getHighestAckedSeqNum());
        readerProxy.ackedChanges(1110);
        Assertions.assertEquals("[]", readerProxy.requestedChanges().toString());
        Assertions.assertEquals(1110, readerProxy.getHighestAckedSeqNum());
    }
}
