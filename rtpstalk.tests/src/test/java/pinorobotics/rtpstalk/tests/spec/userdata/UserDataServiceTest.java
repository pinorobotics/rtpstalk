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
package pinorobotics.rtpstalk.tests.spec.userdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import id.xfunction.PreconditionException;
import id.xfunction.concurrent.SameThreadExecutorService;
import id.xfunction.concurrent.flow.SimpleSubscriber;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.SubscriberDetails;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.userdata.UserDataService;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.TestRtpsNetworkInterface;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannelFactory;
import pinorobotics.rtpstalk.tests.spec.transport.TestRtpsMessageReceiverFactory;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class UserDataServiceTest {

    private static final RtpsTalkConfiguration CONFIG =
            TestConstants.TEST_CONFIG_BUILDER
                    .publisherExecutor(new SameThreadExecutorService())
                    .publisherMaxBufferSize(1)
                    .build();

    @Test
    public void test_subscribe() throws IOException {
        var dataFactory = new TestDataObjectsFactory(true);
        var receiverFactory = new TestRtpsMessageReceiverFactory();
        try (var service =
                new UserDataService(
                        CONFIG,
                        new TestDataChannelFactory(CONFIG),
                        dataFactory,
                        receiverFactory); ) {
            var counters = new int[2];
            class MySubscriber extends SimpleSubscriber<RawData> {
                @Override
                public void onSubscribe(Subscription subscription) {
                    counters[0]++;
                }

                @Override
                public void onError(Throwable throwable) {
                    counters[1]++;
                    throw new RuntimeException(throwable);
                }
            }
            service.start(new TracingToken("test"), new TestRtpsNetworkInterface());
            service.subscribeToRemoteWriter(
                    TestConstants.TEST_READER_ENTITY_ID,
                    List.of(),
                    new Guid(
                            TestConstants.TEST_REMOTE_GUID_PREFIX,
                            EntityId.Predefined.ENTITYID_PARTICIPANT),
                    new SubscriberDetails(null, new MySubscriber()));
            var subscriber = new SubscriberDetails(null, new MySubscriber());
            service.subscribeToRemoteWriter(
                    TestConstants.TEST_READER_ENTITY_ID,
                    List.of(),
                    new Guid(
                            TestConstants.TEST_REMOTE_GUID_PREFIX,
                            EntityId.Predefined.ENTITYID_PARTICIPANT),
                    subscriber);
            assertThrows(
                    PreconditionException.class,
                    () ->
                            service.subscribeToRemoteWriter(
                                    TestConstants.TEST_READER_ENTITY_ID,
                                    List.of(),
                                    new Guid(
                                            TestConstants.TEST_REMOTE_GUID_PREFIX,
                                            EntityId.Predefined.ENTITYID_PARTICIPANT),
                                    subscriber));
            assertEquals(2, counters[0]);
            assertEquals(0, counters[1]);
            assertEquals(2, dataFactory.getReaders().get(0).getSubscribeCount());
            assertEquals(1, receiverFactory.getReceivers().get(0).getSubscribeCount());
        }
    }

    @Test
    public void test_publish() throws IOException {
        var dataFactory = new TestDataObjectsFactory(true);
        var receiverFactory = new TestRtpsMessageReceiverFactory();
        try (var service =
                new UserDataService(
                        CONFIG,
                        new TestDataChannelFactory(CONFIG),
                        dataFactory,
                        receiverFactory); ) {
            service.start(new TracingToken("test"), new TestRtpsNetworkInterface());
            service.publish(
                    new EntityId(1, EntityKind.WRITER_NO_KEY),
                    TestConstants.TEST_READER_ENTITY_ID,
                    new SubmissionPublisher<RawData>());
            var writerEntityId = new EntityId(2, EntityKind.WRITER_NO_KEY);
            service.publish(
                    writerEntityId,
                    TestConstants.TEST_READER_ENTITY_ID,
                    new SubmissionPublisher<RawData>());
            assertThrows(
                    PreconditionException.class,
                    () ->
                            service.publish(
                                    writerEntityId,
                                    TestConstants.TEST_READER_ENTITY_ID,
                                    new SubmissionPublisher<RawData>()));
            assertEquals(1, receiverFactory.getReceivers().get(0).getSubscribeCount());
        }
    }
}
