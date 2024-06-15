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
import id.pubsubtests.data.ByteMessageFactory;
import id.pubsubtests.data.Message;
import id.pubsubtests.data.MessageFactory;
import id.xfunction.XByte;
import id.xfunction.concurrent.flow.TransformPublisher;
import id.xfunction.concurrent.flow.TransformSubscriber;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.WriterSettings;
import pinorobotics.rtpstalk.messages.Parameters;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.qos.DurabilityType;
import pinorobotics.rtpstalk.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.qos.ReliabilityType;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsTalkTestPubSubClient implements TestPubSubClient {

    public static final int QUEUE_SIZE = 10;
    private static final short LENGTH_KEY = 0xbe;
    private static final MessageFactory MESSAGE_FACTORY = new ByteMessageFactory();
    private RtpsTalkClient client =
            new RtpsTalkClient(
                    new RtpsTalkConfiguration.Builder()
                            .historyCacheMaxSize(QUEUE_SIZE)
                            .publisherMaxBufferSize(QUEUE_SIZE)
                            .build());
    private PublisherQosPolicy publisherQosPolicy =
            new PublisherQosPolicy(
                    ReliabilityType.RELIABLE, DurabilityType.VOLATILE_DURABILITY_QOS);
    private WriterSettings writerSettings = new WriterSettings();

    public RtpsTalkTestPubSubClient() {}

    public RtpsTalkTestPubSubClient(PublisherQosPolicy publisherQosPolicy) {
        this.publisherQosPolicy = publisherQosPolicy;
    }

    public RtpsTalkTestPubSubClient(
            RtpsTalkConfiguration configuration, WriterSettings writerSettings) {
        client = new RtpsTalkClient(configuration);
        this.writerSettings = writerSettings;
    }

    public RtpsTalkTestPubSubClient(RtpsTalkConfiguration configuration) {
        client = new RtpsTalkClient(configuration);
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public void subscribe(String topic, Subscriber<Message> subscriber) {
        var transformer = new TransformSubscriber<>(subscriber, this::extractUserdata);
        client.subscribe(topic, asType(topic), transformer);
    }

    private Optional<Message> extractUserdata(RtpsTalkDataMessage message) {
        var length =
                XByte.toInt(message.userInlineQos().orElseThrow().getParameters().get(LENGTH_KEY));
        var userData = Arrays.copyOf(message.data().orElseThrow(), length);
        return Optional.of(MESSAGE_FACTORY.create(userData));
    }

    private Optional<RtpsTalkDataMessage> packUserdata(Message msg) {
        var data = msg.getBody();
        return Optional.of(
                new RtpsTalkDataMessage(
                        new Parameters(Map.of(LENGTH_KEY, XByte.copyToByteArray(data.length))),
                        data));
    }

    private String asType(String topic) {
        return topic + "Type";
    }

    @Override
    public void publish(String topic, Publisher<Message> publisher) {
        var transformer = new TransformPublisher<>(publisher, this::packUserdata);
        client.publish(topic, asType(topic), publisherQosPolicy, writerSettings, transformer);
    }
}
