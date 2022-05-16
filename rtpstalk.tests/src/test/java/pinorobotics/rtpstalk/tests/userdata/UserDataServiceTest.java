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
package pinorobotics.rtpstalk.tests.userdata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import id.xfunction.concurrent.SameThreadExecutorService;
import id.xfunction.concurrent.flow.SimpleSubscriber;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Flow.Subscription;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.discovery.spdp.TestDataChannelFactory;
import pinorobotics.rtpstalk.tests.transport.TestRtpsMessageReceiverFactory;
import pinorobotics.rtpstalk.userdata.UserDataService;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class UserDataServiceTest {

    private static final RtpsTalkConfiguration CONFIG =
            TestConstants.TEST_CONFIG_BUILDER
                    .publisherExecutor(new SameThreadExecutorService())
                    .publisherMaxBufferSize(1)
                    .build();

    @Test
    public void test_subscribe_only_once() throws IOException {
        var dataFactory = new TestDataObjectsFactory(true);
        var receiverFactory = new TestRtpsMessageReceiverFactory();
        try (var service =
                new UserDataService(
                        CONFIG,
                        new TestDataChannelFactory(CONFIG),
                        dataFactory,
                        receiverFactory); ) {
            var counters = new int[2];
            service.start(new TracingToken("test"), TestConstants.TEST_NETWORK_IFACE);
            var subscriber =
                    new SimpleSubscriber<RawData>() {
                        @Override
                        public void onSubscribe(Subscription subscription) {
                            counters[0]++;
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            counters[1]++;
                            throw new RuntimeException(throwable);
                        }
                    };
            service.subscribeToRemoteWriter(
                    TestConstants.TEST_READER_ENTITY_ID,
                    List.of(),
                    new Guid(
                            TestConstants.TEST_REMOTE_GUID_PREFIX,
                            EntityId.Predefined.ENTITYID_PARTICIPANT),
                    subscriber);
            service.subscribeToRemoteWriter(
                    TestConstants.TEST_READER_ENTITY_ID,
                    List.of(),
                    new Guid(
                            TestConstants.TEST_REMOTE_GUID_PREFIX,
                            EntityId.Predefined.ENTITYID_PARTICIPANT),
                    subscriber);
            service.subscribeToRemoteWriter(
                    TestConstants.TEST_READER_ENTITY_ID,
                    List.of(),
                    new Guid(
                            TestConstants.TEST_REMOTE_GUID_PREFIX,
                            EntityId.Predefined.ENTITYID_PARTICIPANT),
                    subscriber);
            assertEquals(1, counters[0]);
            assertEquals(0, counters[1]);
            assertEquals(1, dataFactory.getReaders().get(0).getSubscribeCount());
            assertEquals(1, receiverFactory.getReceivers().get(0).getSubscribeCount());
        }
    }
}
