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
package pinorobotics.rtpstalk.tests.spec.messages.submessages.elements;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class ProtocolVersionTest {

    @ParameterizedTest
    @CsvSource({
        "1,2,false",
        "3,2,false",
        "2,2,true",
        "2,222,true",
    })
    public void test_isCompatible(int major, int minor, boolean isCompatible) {
        Assertions.assertEquals(
                isCompatible, ProtocolVersion.isCompatible(new ProtocolVersion(major, minor)));
    }
}
