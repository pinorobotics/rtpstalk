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
import id.xfunction.logging.TracingToken;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.Executor;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.LocatorKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;

/**
 * @author lambdaprime intid@protonmail.com
 */
public interface TestConstants {

    GuidPrefix TEST_GUID_PREFIX = new GuidPrefix(XByte.fromHex("cafe3d7efd6c2e0b46d2ee00"));
    EntityId TEST_READER_ENTITY_ID = new EntityId(1, EntityKind.READER_NO_KEY);
    EntityId TEST_WRITER_ENTITY_ID = new EntityId(2, EntityKind.WRITER_NO_KEY);
    RtpsTalkConfiguration.Builder TEST_CONFIG_BUILDER =
            new RtpsTalkConfiguration.Builder().guidPrefix(TEST_GUID_PREFIX.value);
    RtpsTalkConfigurationInternal TEST_CONFIG_INTERNAL =
            new RtpsTalkConfigurationInternal(TEST_CONFIG_BUILDER.build());
    RtpsTalkConfiguration TEST_CONFIG = TEST_CONFIG_INTERNAL.publicConfig();

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
    TestRtpsNetworkInterface TEST_NETWORK_IFACE = new TestRtpsNetworkInterface();
    NetworkInterface LOOPBACK_NETWORK_IFACE = Unchecked.get(() -> NetworkInterface.getByName("lo"));
    TracingToken TEST_TRACING_TOKEN = new TracingToken("TEST");
    Executor TEST_PUBLISHER_EXECUTOR =
            RtpsTalkConfiguration.Builder.DEFAULT_PUBLISHER_EXECUTOR.get();
}
