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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.transport.io.LengthCalculator;

/** @author lambdaprime intid@protonmail.com */
public class LengthCalculatorTest {

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
    }
}
