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
package pinorobotics.rtpstalk.tests.integration.pubsubtests;

import id.xfunction.concurrent.flow.SimpleSubscriber;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author lambdaprime intid@protonmail.com
 */
public abstract class PubSubClientTests {

    public record TestCase(Supplier<TestPubSubClient> clientFactory) {}

    /**
     * Test that publisher does not drop messages when there is no subscribers and will block
     * eventually accepting new ones,
     */
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void test_publish_when_no_subscribers(TestCase testCase) {
        try (var publisherClient = testCase.clientFactory.get(); ) {
            String topic = "testTopic1";
            var publisher = new SubmissionPublisher<String>();
            String data = "hello";
            publisherClient.publish(topic, publisher);
            while (publisher.offer(data, null) >= 0)
                ;
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void test_publish_forever(TestCase testCase)
            throws InterruptedException, ExecutionException {
        try (var subscriberClient = testCase.clientFactory.get();
                var publisherClient = testCase.clientFactory.get(); ) {
            var future = new CompletableFuture<String>();
            String topic = "testTopic1";
            var publisher = new SubmissionPublisher<String>();
            String data = "hello";
            publisherClient.publish(topic, publisher);
            subscriberClient.subscribe(
                    topic,
                    new SimpleSubscriber<String>() {
                        @Override
                        public void onNext(String item) {
                            System.out.println(item);
                            getSubscription().get().cancel();
                            future.complete(item);
                        }
                    });
            ForkJoinPool.commonPool()
                    .execute(
                            () -> {
                                while (!future.isDone()) {
                                    publisher.submit(data);
                                }
                            });
            Assertions.assertEquals(data, future.get());
        }
    }
}
