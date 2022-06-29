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
package pinorobotics.rtpstalk.tests.spec.structure.history;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.structure.history.CacheChange;
import pinorobotics.rtpstalk.impl.spec.structure.history.HistoryCache;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class HistoryCacheTest {

    @Test
    public void test_out_of_order() {
        var cache = new HistoryCache<RtpsTalkDataMessage>();
        var guid =
                new Guid(
                        TestConstants.TEST_GUID_PREFIX,
                        EntityId.Predefined.ENTITYID_PARTICIPANT.getValue());
        Assertions.assertEquals(
                true, cache.addChange(new CacheChange<RtpsTalkDataMessage>(guid, 1, null)));
        Assertions.assertEquals(
                true, cache.addChange(new CacheChange<RtpsTalkDataMessage>(guid, 3, null)));
        Assertions.assertEquals(
                false, cache.addChange(new CacheChange<RtpsTalkDataMessage>(guid, 2, null)));
        Assertions.assertEquals(
                false, cache.addChange(new CacheChange<RtpsTalkDataMessage>(guid, 3, null)));
    }
}
