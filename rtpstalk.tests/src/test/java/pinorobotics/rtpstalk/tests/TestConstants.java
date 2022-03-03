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
package pinorobotics.rtpstalk.tests;

import id.xfunction.XByte;
import id.xfunction.function.Unchecked;
import java.net.InetAddress;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.LocatorKind;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;

/** @author lambdaprime intid@protonmail.com */
@SuppressWarnings("exports")
public interface TestConstants {

    GuidPrefix TEST_GUID_PREFIX = new GuidPrefix(XByte.fromHex("cafe3d7efd6c2e0b46d2ee00"));
    RtpsTalkConfiguration TEST_CONFIG =
            new RtpsTalkConfiguration().withGuidPrefix(TEST_GUID_PREFIX);

    GuidPrefix TEST_REMOTE_GUID_PREFIX = new GuidPrefix(XByte.fromHex("010f70b7fb013df101000000"));
    InetAddress TEST_REMOTE_ADDRESS = Unchecked.get(() -> InetAddress.getByName("33.3.3.3"));
    Locator TEST_REMOTE_METATRAFFIC_UNICAST_LOCATOR =
            new Locator(LocatorKind.LOCATOR_KIND_UDPv4, 7012, TEST_REMOTE_ADDRESS);
    Locator TEST_REMOTE_DEFAULT_UNICAST_LOCATOR =
            new Locator(LocatorKind.LOCATOR_KIND_UDPv4, 7013, TEST_REMOTE_ADDRESS);

    RtpsMessage TEST_MESSAGE_INFODST_ACKNACK =
            new RtpsMessage(
                    new Header(
                            ProtocolId.Predefined.RTPS.getValue(),
                            ProtocolVersion.Predefined.Version_2_3.getValue(),
                            VendorId.Predefined.RTPSTALK.getValue(),
                            TEST_GUID_PREFIX),
                    new InfoDestination(TEST_REMOTE_GUID_PREFIX),
                    new AckNack(
                            EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR
                                    .getValue(),
                            EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER
                                    .getValue(),
                            new SequenceNumberSet(1, 9, 511),
                            new Count()));
}
