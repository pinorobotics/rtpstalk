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

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsTalkHelloWorldClient {

    private RtpsTalkClient client;

    public RtpsTalkHelloWorldClient() {
        this(new RtpsTalkConfiguration.Builder().build());
    }

    public RtpsTalkHelloWorldClient(RtpsTalkConfiguration config) {
        client = new RtpsTalkClient(config);
    }

    public void start() {
        client.start();
    }

    public void close() {
        client.close();
    }

    public void publishToHelloWorld(String topic, Publisher<RtpsTalkDataMessage> publisher) {
        client.publish(topic, HelloWorldConfig.DEFAULT_TOPIC_TYPE, publisher);
    }

    public int subscribeToHelloWorld(String topic, Subscriber<RtpsTalkDataMessage> subscriber) {
        return client.subscribe(topic, HelloWorldConfig.DEFAULT_TOPIC_TYPE, subscriber);
    }
}
