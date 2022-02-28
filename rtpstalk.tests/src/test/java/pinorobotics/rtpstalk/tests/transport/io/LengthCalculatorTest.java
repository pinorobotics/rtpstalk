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
package pinorobotics.rtpstalk.tests.transport.io;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.transport.io.LengthCalculator;

/** @author lambdaprime intid@protonmail.com */
public class LengthCalculatorTest {

    private static final Guid TEST_GUID_PUBLICATIONS_ANNOUNCER =
            new Guid(
                    GuidPrefix.generate(),
                    EntityId.Predefined.ENTITYID_SEDP_BUILTIN_PUBLICATIONS_ANNOUNCER.getValue());
    private static final ParameterList TEST_PARAMETER_LIST = createTestParameterList();

    @Test
    public void test_calculateLength() {
        // fixed length
        Assertions.assertEquals(
                4,
                LengthCalculator.getInstance()
                        .calculateLength(EntityId.Predefined.ENTITYID_PARTICIPANT.getValue()));
        // variable length
        Assertions.assertEquals(
                21, LengthCalculator.getInstance().calculateLength("very long string"));

        // parameter list
        Assertions.assertEquals(
                56, LengthCalculator.getInstance().calculateLength(TEST_PARAMETER_LIST));
    }

    private static ParameterList createTestParameterList() {
        var params = new LinkedHashMap<ParameterId, Object>();
        params.put(ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue());
        params.put(ParameterId.PID_TOPIC_NAME, "long topic name");
        params.put(ParameterId.PID_PARTICIPANT_GUID, TEST_GUID_PUBLICATIONS_ANNOUNCER);
        return new ParameterList(params);
    }

    @Test
    public void test_calculateParameterValueLength() {
        Assertions.assertEquals(
                20,
                LengthCalculator.getInstance()
                        .calculateParameterValueLength(
                                Map.entry(ParameterId.PID_TOPIC_NAME, "long topic name")));
    }
}
