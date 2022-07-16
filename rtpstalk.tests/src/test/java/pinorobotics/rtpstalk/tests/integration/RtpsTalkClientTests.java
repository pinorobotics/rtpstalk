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
package pinorobotics.rtpstalk.tests.integration;

import id.pubsubtests.PubSubClientTests;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import pinorobotics.rtpstalk.tests.LogUtils;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsTalkClientTests extends PubSubClientTests {

    static Stream<TestCase> dataProvider() {
        return Stream.of(new TestCase(RtpsTalkTestPubSubClient::new));
    }

    @BeforeEach
    public void setup() throws IOException {
        LogUtils.setupLog();
    }
}
