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
package pinorobotics.rtpstalk.tests;

import static pinorobotics.rtpstalk.tests.TestConstants.TEST_GUID_PREFIX;

import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannel;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class TestRtpsNetworkInterface extends RtpsNetworkInterface {

    public TestRtpsNetworkInterface() {
        super(
                TestConstants.TEST_TRACING_TOKEN,
                new TestDataChannel(TEST_GUID_PREFIX, true),
                TestConstants.TEST_DEFAULT_UNICAST_LOCATOR,
                new TestDataChannel(TEST_GUID_PREFIX, true),
                TestConstants.TEST_METATRAFFIC_UNICAST_LOCATOR);
    }
}
