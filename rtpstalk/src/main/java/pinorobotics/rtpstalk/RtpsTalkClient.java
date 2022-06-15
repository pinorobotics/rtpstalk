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

import id.xfunction.Preconditions;
import id.xfunction.concurrent.flow.TransformProcessor;
import id.xfunction.logging.XLogger;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.RtpsServiceManager;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiverFactory;

/** @author lambdaprime intid@protonmail.com */
public class RtpsTalkClient implements AutoCloseable {

    private XLogger logger;
    private RtpsTalkConfiguration config;
    private DataChannelFactory channelFactory;
    private RtpsServiceManager serviceManager;
    private boolean isStarted;
    private boolean isClosed;
    private TracingToken tracingToken;

    public RtpsTalkClient() {
        this(new RtpsTalkConfiguration.Builder().build());
    }

    public RtpsTalkClient(RtpsTalkConfiguration config) {
        this.config = config;
        channelFactory = new DataChannelFactory(config);
        serviceManager =
                new RtpsServiceManager(config, channelFactory, new RtpsMessageReceiverFactory());
    }

    public RtpsTalkClient(RtpsTalkConfiguration config, String clientName) {
        this(config);
        tracingToken = new TracingToken(clientName);
    }

    public void subscribe(String topic, String type, Subscriber<byte[]> subscriber) {
        if (!isStarted) {
            start();
        }
        var transformer = new TransformProcessor<>(RawData::getData);
        transformer.subscribe(subscriber);
        serviceManager.subscribe(topic, type, transformer);
    }

    public void publish(String topic, String type, Publisher<byte[]> publisher) {
        if (!isStarted) {
            start();
        }
        var transformer = new TransformProcessor<byte[], RawData>(RawData::new);
        serviceManager.publish(topic, type, transformer);
        publisher.subscribe(transformer);
    }

    /**
     * Starts all RTPS services which are required for communication with other RTPS participants.
     * Among them:
     *
     * <ul>
     *   <li>Simple Participant Discovery Protocol (SPDP)
     *   <li>Simple Endpoint Discovery Protocol (SEDP)
     *   <li>User data
     *   <li>...
     * </ul>
     *
     * <p><b>rtpstalk</b> client is lazy by default and starts all services automatically when user
     * initiates some RTPS interactions.
     *
     * <p>Using this method users can start all services on demand.
     */
    public void start() {
        Preconditions.isTrue(!isStarted, "Already started");
        if (tracingToken == null) {
            tracingToken = new TracingToken("" + hashCode());
        }
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
        logger.entering("start");
        logger.fine("Using following configuration: {0}", config);
        serviceManager.startAll(tracingToken);
        isStarted = true;
        logger.exiting("start");
    }

    @Override
    public void close() {
        if (!isStarted) return;
        if (isClosed) return;
        serviceManager.close();
        logger.fine("Closed");
    }

    public RtpsTalkConfiguration getConfiguration() {
        return config;
    }
}
