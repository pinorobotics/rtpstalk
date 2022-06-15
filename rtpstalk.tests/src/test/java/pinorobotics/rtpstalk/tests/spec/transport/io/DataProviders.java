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
package pinorobotics.rtpstalk.tests.spec.transport.io;

import static pinorobotics.rtpstalk.tests.TestConstants.*;

import id.xfunction.ResourceUtils;
import java.util.List;
import java.util.stream.Stream;
import pinorobotics.rtpstalk.impl.spec.messages.Header;
import pinorobotics.rtpstalk.impl.spec.messages.ProtocolId;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.AckNack;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Timestamp;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;

/** @author lambdaprime intid@protonmail.com */
public class DataProviders {

    private static final ResourceUtils resourceUtils = new ResourceUtils();

    public static Stream<List> rtpsMessageConversion() {
        return Stream.of(
                List.of(
                        resourceUtils.readResource(DataProviders.class, "test1"),
                        new RtpsMessage(
                                new Header(
                                        ProtocolId.Predefined.RTPS.getValue(),
                                        ProtocolVersion.Predefined.Version_2_3.getValue(),
                                        VendorId.Predefined.RTPSTALK.getValue(),
                                        TEST_GUID_PREFIX),
                                new InfoDestination(TEST_REMOTE_GUID_PREFIX),
                                new AckNack(
                                        EntityId.Predefined
                                                .ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR
                                                .getValue(),
                                        EntityId.Predefined
                                                .ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER
                                                .getValue(),
                                        new SequenceNumberSet(1, 9, 511),
                                        new Count()))),
                List.of(
                        resourceUtils.readResource(DataProviders.class, "test_submessages_padding"),
                        new RtpsMessage(
                                new Header(
                                        ProtocolId.Predefined.RTPS.getValue(),
                                        ProtocolVersion.Predefined.Version_2_3.getValue(),
                                        VendorId.Predefined.RTPSTALK.getValue(),
                                        TEST_GUID_PREFIX),
                                new InfoDestination(TEST_REMOTE_GUID_PREFIX),
                                new Data(
                                        EntityId.Predefined
                                                .ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR,
                                        EntityId.Predefined
                                                .ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER,
                                        new SequenceNumber(1),
                                        new SerializedPayload(
                                                new RawData(new byte[] {0x11, 0x22}))))),
                List.of(
                        resourceUtils.readResource(DataProviders.class, "test_multiple_data"),
                        new RtpsMessage(
                                new Header(
                                        ProtocolId.Predefined.RTPS.getValue(),
                                        ProtocolVersion.Predefined.Version_2_3.getValue(),
                                        VendorId.Predefined.RTPSTALK.getValue(),
                                        TEST_GUID_PREFIX),
                                new InfoTimestamp(new Timestamp(1654495356, 0)),
                                new Data(
                                        new EntityId(0x12, EntityKind.READER_NO_KEY),
                                        new EntityId(0x01, EntityKind.WRITER_NO_KEY),
                                        new SequenceNumber(1),
                                        new SerializedPayload(
                                                new RawData(
                                                        new byte[] {
                                                            0x02, 0x00, 0x00, 0x00, 0x30, 0x00
                                                        }))),
                                new Data(
                                        new EntityId(0x12, EntityKind.READER_NO_KEY),
                                        new EntityId(0x01, EntityKind.WRITER_NO_KEY),
                                        new SequenceNumber(2),
                                        new SerializedPayload(
                                                new RawData(
                                                        new byte[] {
                                                            0x02, 0x00, 0x00, 0x00, 0x31, 0x00
                                                        }))),
                                new Data(
                                        new EntityId(0x12, EntityKind.READER_NO_KEY),
                                        new EntityId(0x01, EntityKind.WRITER_NO_KEY),
                                        new SequenceNumber(3),
                                        new SerializedPayload(
                                                new RawData(
                                                        new byte[] {
                                                            0x02, 0x00, 0x00, 0x00, 0x32, 0x00
                                                        }))))));
    }
}
