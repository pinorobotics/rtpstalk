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
package pinorobotics.rtpstalk.tests.behavior.reader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.behavior.reader.FilterByEntityIdRtpsSubmessageVisitor;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.walk.Result;
import pinorobotics.rtpstalk.impl.spec.messages.walk.RtpsSubmessageVisitor;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class FilterByEntityIdRtpsSubmessageVisitorTest {

    @Test
    public void test_onData_onHeartbeat() {
        var visited = new int[2];
        var filter =
                new FilterByEntityIdRtpsSubmessageVisitor(
                        TestConstants.TEST_READER_ENTITY_ID,
                        new RtpsSubmessageVisitor() {
                            @Override
                            public Result onData(GuidPrefix guidPrefix, Data data) {
                                visited[0]++;
                                return Result.CONTINUE;
                            }

                            @Override
                            public Result onHeartbeat(GuidPrefix guidPrefix, Heartbeat heartbeat) {
                                visited[1]++;
                                return Result.CONTINUE;
                            }
                        });
        filter.onData(
                TestConstants.TEST_GUID_PREFIX, createData(TestConstants.TEST_READER_ENTITY_ID));
        filter.onData(
                TestConstants.TEST_GUID_PREFIX, createData(TestConstants.TEST_WRITER_ENTITY_ID));
        filter.onData(
                TestConstants.TEST_GUID_PREFIX,
                createData(EntityId.Predefined.ENTITYID_UNKNOWN.getValue()));

        filter.onHeartbeat(
                TestConstants.TEST_GUID_PREFIX,
                createHeartbeat(TestConstants.TEST_READER_ENTITY_ID));
        filter.onHeartbeat(
                TestConstants.TEST_GUID_PREFIX,
                createHeartbeat(TestConstants.TEST_WRITER_ENTITY_ID));
        filter.onHeartbeat(
                TestConstants.TEST_GUID_PREFIX,
                createHeartbeat(EntityId.Predefined.ENTITYID_UNKNOWN.getValue()));

        Assertions.assertEquals(2, visited[0]);
        Assertions.assertEquals(2, visited[1]);
    }

    private Heartbeat createHeartbeat(EntityId readerEntityId) {
        return new Heartbeat(
                readerEntityId, null, new SequenceNumber(1), new SequenceNumber(2), new Count(1));
    }

    private Data createData(EntityId readerEntityId) {
        return new Data(
                readerEntityId,
                null,
                new SequenceNumber(1),
                new SerializedPayload(new RawData(new byte[0]), false));
    }
}
