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
package pinorobotics.rtpstalk.tests.spec.structure.history;

import static pinorobotics.rtpstalk.tests.TestConstants.TEST_GUID_READER;
import static pinorobotics.rtpstalk.tests.TestConstants.TEST_TRACING_TOKEN;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.structure.history.CacheChange;
import pinorobotics.rtpstalk.impl.spec.structure.history.WriterChanges;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class WriterChangesTest {

    @Test
    public void test() {
        var changes = new WriterChanges<RtpsTalkDataMessage>(TEST_TRACING_TOKEN);

        Assertions.assertEquals(0, changes.getNumberOfChanges());
        Assertions.assertEquals(SequenceNumber.MIN.value, changes.getSeqNumMin());
        Assertions.assertEquals(SequenceNumber.MIN.value, changes.getSeqNumMax());
        Assertions.assertEquals(false, changes.containsChange(11));
        Assertions.assertEquals("[]", changes.getAllSortedBySeqNum().toList().toString());
        Assertions.assertEquals("[]", changes.getAllSortedBySeqNum(4).toList().toString());

        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 11, null));
        Assertions.assertEquals(1, changes.getNumberOfChanges());
        Assertions.assertEquals(11, changes.getSeqNumMin());
        Assertions.assertEquals(11, changes.getSeqNumMax());
        Assertions.assertEquals(true, changes.containsChange(11));
        Assertions.assertEquals("[11]", toString(changes.getAllSortedBySeqNum()));
        Assertions.assertEquals("[11]", toString(changes.getAllSortedBySeqNum(0)));
        Assertions.assertEquals("[11]", toString(changes.getAllSortedBySeqNum(4)));
        Assertions.assertEquals("[]", toString(changes.getAllSortedBySeqNum(11)));

        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 11, null));
        Assertions.assertEquals(1, changes.getNumberOfChanges());
        Assertions.assertEquals(11, changes.getSeqNumMin());
        Assertions.assertEquals(11, changes.getSeqNumMax());
        Assertions.assertEquals("[11]", toString(changes.getAllSortedBySeqNum()));

        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 12, null));
        Assertions.assertEquals(2, changes.getNumberOfChanges());
        Assertions.assertEquals(11, changes.getSeqNumMin());
        Assertions.assertEquals(12, changes.getSeqNumMax());
        Assertions.assertEquals(true, changes.containsChange(12));
        Assertions.assertEquals("[11, 12]", toString(changes.getAllSortedBySeqNum()));
        Assertions.assertEquals("[11, 12]", toString(changes.getAllSortedBySeqNum(5)));
        Assertions.assertEquals("[12]", toString(changes.getAllSortedBySeqNum(11)));

        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 13, null));
        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 14, null));
        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 14, null));
        Assertions.assertEquals(4, changes.getNumberOfChanges());
        Assertions.assertEquals(11, changes.getSeqNumMin());
        Assertions.assertEquals(14, changes.getSeqNumMax());
        Assertions.assertEquals("[11, 12, 13, 14]", toString(changes.getAllSortedBySeqNum()));

        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 17, null));
        Assertions.assertEquals(5, changes.getNumberOfChanges());
        Assertions.assertEquals(11, changes.getSeqNumMin());
        Assertions.assertEquals(17, changes.getSeqNumMax());
        Assertions.assertEquals("[11, 12, 13, 14, 17]", toString(changes.getAllSortedBySeqNum()));
        Assertions.assertEquals("[17]", toString(changes.getAllSortedBySeqNum(15)));

        changes.removeAllBelow(5);
        Assertions.assertEquals(5, changes.getNumberOfChanges());
        Assertions.assertEquals(11, changes.getSeqNumMin());
        Assertions.assertEquals(17, changes.getSeqNumMax());
        Assertions.assertEquals("[11, 12, 13, 14, 17]", toString(changes.getAllSortedBySeqNum()));
        Assertions.assertEquals("[17]", toString(changes.getAllSortedBySeqNum(15)));

        changes.removeAllBelow(13);
        Assertions.assertEquals(3, changes.getNumberOfChanges());
        Assertions.assertEquals(13, changes.getSeqNumMin());
        Assertions.assertEquals(17, changes.getSeqNumMax());
        Assertions.assertEquals("[13, 14, 17]", toString(changes.getAllSortedBySeqNum()));
        Assertions.assertEquals("[17]", toString(changes.getAllSortedBySeqNum(15)));

        changes.removeAllBelow(16);
        Assertions.assertEquals(1, changes.getNumberOfChanges());
        Assertions.assertEquals(16, changes.getSeqNumMin());
        Assertions.assertEquals(17, changes.getSeqNumMax());
        Assertions.assertEquals("[17]", toString(changes.getAllSortedBySeqNum()));
        Assertions.assertEquals("[17]", toString(changes.getAllSortedBySeqNum(15)));
    }

    @Test
    public void test_findAll() {
        var changes = new WriterChanges<RtpsTalkDataMessage>(TEST_TRACING_TOKEN);

        Assertions.assertEquals(
                "[]", toString(changes.findAll(List.of(1L, 2L, 3L, 10L, 12L, 15L))));

        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 14, null));
        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 11, null));
        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 13, null));
        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 12, null));
        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 15, null));

        Assertions.assertEquals(
                "[12, 15]", toString(changes.findAll(List.of(1L, 2L, 3L, 10L, 12L, 15L))));
        Assertions.assertEquals(
                "[12, 15]", toString(changes.findAll(List.of(12L, 10L, 15L, 1L, 2L, 3L))));
        Assertions.assertEquals(
                "[11, 12, 13, 14, 15]",
                toString(changes.findAll(List.of(11L, 12L, 13L, 14L, 15L))));
        Assertions.assertEquals("[]", toString(changes.findAll(List.of())));

        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 134567, null));
        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 1129999, null));
        changes.addChange(new CacheChange<RtpsTalkDataMessage>(TEST_GUID_READER, 97465223, null));
        Assertions.assertEquals(
                "[1129999, 97465223]", toString(changes.findAll(List.of(97465223L, 1129999L))));
    }

    private String toString(Stream<CacheChange<RtpsTalkDataMessage>> changes) {
        return changes.map(CacheChange::getSequenceNumber).toList().toString();
    }
}
