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
package pinorobotics.rtpstalk.tests.spec.transport;

import id.xfunction.XByte;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.LocatorKind;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataChannelFactoryTest {
    @Test
    public void test() throws UnknownHostException {
        var addr = InetAddress.getByAddress(XByte.copyAsByteLiterals(0x1, 0x1, 0x1, 0x1));
        var locator =
                new DataChannelFactory(TestConstants.TEST_TRACING_TOKEN, null)
                        .findLocator(
                                List.of(
                                        new Locator(LocatorKind.LOCATOR_KIND_INVALID, 0, addr),
                                        new Locator(LocatorKind.LOCATOR_KIND_UDPv4, 1230, addr)));

        Assertions.assertEquals(
                """
                Optional[{ "transportType": "LOCATOR_KIND_UDPv4", "port": "1230", "address": "/1.1.1.1" }]""",
                locator.toString());
    }
}
