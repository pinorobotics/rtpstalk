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
package pinorobotics.rtpstalk;

import id.xfunction.XAsserts;
import id.xfunction.concurrent.flow.TransformProcessor;
import id.xfunction.logging.XLogger;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.transport.DataChannelFactory;

/** @author lambdaprime intid@protonmail.com */
public class RtpsTalkClient {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsTalkClient.class);
    private RtpsTalkConfiguration config;
    private DataChannelFactory channelFactory;
    private RtpsServiceManager serviceManager;
    private boolean isStarted;

    public RtpsTalkClient() {
        this(new RtpsTalkConfiguration.Builder().build());
    }

    public RtpsTalkClient(RtpsTalkConfiguration config) {
        this.config = config;
        channelFactory = new DataChannelFactory(config);
        serviceManager = new RtpsServiceManager(config, channelFactory);
    }

    public void subscribe(String topic, String type, Subscriber<byte[]> subscriber) {
        if (!isStarted) {
            start();
        }
        var entityId = new EntityId(config.appEntityKey(), EntityKind.READER_NO_KEY);
        var transformer = new TransformProcessor<>(RawData::getData);
        transformer.subscribe(subscriber);
        serviceManager.subscribe(topic, type, transformer, entityId);
    }

    public void publish(String topic, String type, Publisher<byte[]> publisher) {
        if (!isStarted) {
            start();
        }
        var transformer = new TransformProcessor<byte[], RawData>(RawData::new);
        serviceManager.publish(topic, type, transformer);
        publisher.subscribe(transformer);
    }

    private void start() {
        LOGGER.entering("start");
        XAsserts.assertTrue(!isStarted, "Already started");
        LOGGER.fine("Using following configuration: {0}", config);
        serviceManager.startAll();
        isStarted = true;
        LOGGER.exiting("start");
    }
}
