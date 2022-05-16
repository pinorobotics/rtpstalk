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
import java.net.NetworkInterface;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.LocatorKind;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;

/** @author lambdaprime intid@protonmail.com */
public interface TestConstants {

    GuidPrefix TEST_GUID_PREFIX = new GuidPrefix(XByte.fromHex("cafe3d7efd6c2e0b46d2ee00"));
    EntityId TEST_READER_ENTITY_ID = new EntityId(1, EntityKind.READER_NO_KEY);
    RtpsTalkConfiguration.Builder TEST_CONFIG_BUILDER =
            new RtpsTalkConfiguration.Builder().guidPrefix(TEST_GUID_PREFIX);
    RtpsTalkConfiguration TEST_CONFIG = TEST_CONFIG_BUILDER.build();

    InetAddress TEST_ADDRESS = Unchecked.get(() -> InetAddress.getByName("11.1.1.1"));
    Locator TEST_METATRAFFIC_UNICAST_LOCATOR =
            new Locator(LocatorKind.LOCATOR_KIND_UDPv4, 7412, TEST_ADDRESS);
    Locator TEST_DEFAULT_UNICAST_LOCATOR =
            new Locator(LocatorKind.LOCATOR_KIND_UDPv4, 7413, TEST_ADDRESS);
    Locator TEST_DEFAULT_MULTICAST_LOCATOR =
            Locator.createDefaultMulticastLocator(TestConstants.TEST_CONFIG.domainId());

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
    RtpsMessage TEST_MESSAGE_INFODST_DATA_PADDING =
            new RtpsMessage(
                    new Header(
                            ProtocolId.Predefined.RTPS.getValue(),
                            ProtocolVersion.Predefined.Version_2_3.getValue(),
                            VendorId.Predefined.RTPSTALK.getValue(),
                            TEST_GUID_PREFIX),
                    new InfoDestination(TEST_REMOTE_GUID_PREFIX),
                    new Data(
                            EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR,
                            EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER,
                            new SequenceNumber(1),
                            new SerializedPayload(new RawData(new byte[] {0x11, 0x22}))));
    TestRtpsNetworkInterface TEST_NETWORK_IFACE = new TestRtpsNetworkInterface();
    NetworkInterface LOOPBACK_NETWORK_IFACE = Unchecked.get(() -> NetworkInterface.getByName("lo"));
}
