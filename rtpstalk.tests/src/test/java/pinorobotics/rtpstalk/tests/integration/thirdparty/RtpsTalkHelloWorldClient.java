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
package pinorobotics.rtpstalk.tests.integration.thirdparty;

import id.pubsubtests.TestPubSubClient;
import id.pubsubtests.data.ByteMessageFactory;
import id.pubsubtests.data.Message;
import id.pubsubtests.data.MessageFactory;
import id.xfunction.XByte;
import id.xfunction.concurrent.flow.TransformSubscriber;
import java.util.Arrays;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.LogUtils;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsTalkHelloWorldClient implements TestPubSubClient {

    private MessageFactory messageFactory = new ByteMessageFactory();
    private RtpsTalkClient client;

    public RtpsTalkHelloWorldClient() {
        this(new RtpsTalkConfiguration.Builder().build());
    }

    public RtpsTalkHelloWorldClient(RtpsTalkConfiguration config) {
        client = new RtpsTalkClient(config);
    }

    public RtpsTalkHelloWorldClient(StringMessageFactory messageFactory) {
        this();
        this.messageFactory = messageFactory;
    }

    public void start() {
        client.start();
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public void publish(String topic, Publisher<Message> var2) {}

    @Override
    public void subscribe(String topic, Subscriber<Message> subscriber) {
        subscribeToHelloWorld(
                topic,
                new TransformSubscriber<RtpsTalkDataMessage, Message>(
                        subscriber,
                        data -> {
                            return data.data()
                                    .map(
                                            d -> {
                                                d = Arrays.copyOfRange(d, 8, d.length);
                                                System.out.format(
                                                        "RtpsTalkHelloWorldClient subscriber"
                                                                + " received: len=%d [%s]\n",
                                                        d.length,
                                                        LogUtils.ellipsize(XByte.toHexPairs(d)));
                                                return messageFactory.create(d);
                                            });
                        }));
    }

    public void publishToHelloWorld(String topic, Publisher<RtpsTalkDataMessage> publisher) {
        client.publish(topic, HelloWorldConfig.DEFAULT_TOPIC_TYPE, publisher);
    }

    public int subscribeToHelloWorld(String topic, Subscriber<RtpsTalkDataMessage> subscriber) {
        return client.subscribe(topic, HelloWorldConfig.DEFAULT_TOPIC_TYPE, subscriber);
    }
}
