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

import id.pubsubtests.TestPubSubClient;
import id.xfunction.concurrent.SameThreadExecutorService;
import id.xfunction.concurrent.flow.TransformProcessor;
import java.util.Optional;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.qos.DurabilityType;
import pinorobotics.rtpstalk.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.qos.ReliabilityType;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsTalkTestPubSubClient implements TestPubSubClient {

    private RtpsTalkClient client =
            new RtpsTalkClient(
                    new RtpsTalkConfiguration.Builder()
                            .historyCacheMaxSize(10)
                            .publisherMaxBufferSize(10)
                            .build());

    @Override
    public void close() {
        client.close();
    }

    @Override
    public void subscribe(String topic, Subscriber<String> subscriber) {
        var transformer = new TransformProcessor<>(this::extractString);
        transformer.subscribe(subscriber);
        client.subscribe(topic, asType(topic), transformer);
    }

    private Optional<String> extractString(RtpsTalkDataMessage message) {
        return message.data().map(v -> new String(v).trim());
    }

    private Optional<RtpsTalkDataMessage> packString(String message) {
        return Optional.of(new RtpsTalkDataMessage(message.getBytes()));
    }

    private String asType(String topic) {
        return topic + "Type";
    }

    @Override
    public void publish(String topic, Publisher<String> publisher) {
        var transformer =
                new TransformProcessor<>(this::packString, new SameThreadExecutorService(), 1);
        publisher.subscribe(transformer);
        client.publish(
                topic,
                asType(topic),
                new PublisherQosPolicy(
                        ReliabilityType.RELIABLE, DurabilityType.VOLATILE_DURABILITY_QOS),
                transformer);
    }
}
