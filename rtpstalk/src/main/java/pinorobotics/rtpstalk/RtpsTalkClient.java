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
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.impl.RtpsServiceManager;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiverFactory;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.qos.SubscriberQosPolicy;

/**
 * RTPS client
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsTalkClient implements AutoCloseable {

    private XLogger logger;
    private RtpsTalkConfigurationInternal config;
    private DataChannelFactory channelFactory;
    private RtpsServiceManager serviceManager;
    private boolean isStarted;
    private boolean isClosed;
    private TracingToken tracingToken;

    /** Create client with default {@link RtpsTalkConfiguration} */
    public RtpsTalkClient() {
        this(new RtpsTalkConfiguration.Builder().build());
    }

    public RtpsTalkClient(RtpsTalkConfiguration config) {
        this.config = new RtpsTalkConfigurationInternal(config);
        channelFactory = new DataChannelFactory(config);
        serviceManager =
                new RtpsServiceManager(
                        this.config, channelFactory, new RtpsMessageReceiverFactory());
    }

    /**
     * Subscribe to RTPS topic with default {@link SubscriberQosPolicy}
     *
     * @param topic topic name
     * @param type topic type
     * @param subscriber subscriber which will be receiving messages for the given topic
     * @return EntityId assigned to this client DataReader which will be registered to the given
     *     topic
     */
    public int subscribe(String topic, String type, Subscriber<RtpsTalkDataMessage> subscriber) {
        return subscribe(topic, type, new SubscriberQosPolicy(), subscriber);
    }

    /**
     * @see #subscribe
     */
    public int subscribe(
            String topic,
            String type,
            SubscriberQosPolicy policy,
            Subscriber<RtpsTalkDataMessage> subscriber) {
        if (!isStarted) {
            start();
        }
        logger.fine("Subscribing to topic {0} with type {1}, QoS {2}", topic, type, policy);
        var entityId = serviceManager.subscribe(topic, type, policy, subscriber);
        return entityId.value;
    }

    /**
     * Register user owned {@link Publisher} to RTPS topic with default {@link PublisherQosPolicy}.
     *
     * <p>This client subscribes to the given user owned publisher and announces its presence to all
     * RTPS Participants in the network. Each message received by this client from the user owned
     * publisher will be sent only to those Participants which announce their interest in the given
     * topic.
     *
     * <p>Only one publisher allowed per topic.
     *
     * @param topic topic name
     * @param type topic type
     * @param publisher user owned publisher which emits RTPS Data messages for the given topic.
     */
    public void publish(String topic, String type, Publisher<RtpsTalkDataMessage> publisher) {
        publish(topic, type, new PublisherQosPolicy(), publisher);
    }

    /**
     * @see #publish
     */
    public void publish(
            String topic,
            String type,
            PublisherQosPolicy policy,
            Publisher<RtpsTalkDataMessage> publisher) {
        publish(topic, type, policy, new WriterSettings(), publisher);
    }

    /**
     * @see #publish
     */
    public void publish(
            String topic,
            String type,
            PublisherQosPolicy policy,
            WriterSettings writerSettings,
            Publisher<RtpsTalkDataMessage> publisher) {
        if (!isStarted) {
            start();
        }
        logger.fine("Publishing to topic {0} with type {1}", topic, type);
        serviceManager.publish(topic, type, policy, writerSettings, publisher);
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
     * initiates any RTPS interaction.
     *
     * <p>Using this method users can start all RTPS services in advance. It may be useful when you
     * need to assign resources (for example ports) early during application startup.
     */
    public void start() {
        Preconditions.isTrue(!isStarted, "Already started");
        if (tracingToken == null) {
            tracingToken = new TracingToken("" + hashCode());
        }
        logger = XLogger.getLogger(getClass(), tracingToken);
        logger.entering("start");
        logger.fine("Using following configuration: {0}", config);
        serviceManager.startAll(tracingToken);
        isStarted = true;
        logger.exiting("start");
    }

    /** Stop all subscribers, publishers, RTPS services assigned to this client. */
    @Override
    public void close() {
        // User owned Publishers are represented by Flow.Publisher interface and it has no close
        // method
        // It means we are not closing user owned publishers
        if (!isStarted) return;
        if (isClosed) return;
        serviceManager.close();
        logger.fine("Closed");
    }

    /** Return current client configuration */
    public RtpsTalkConfiguration getConfiguration() {
        return config.publicConfig();
    }
}
