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
package pinorobotics.rtpstalk.impl.topics;

import id.xfunction.Preconditions;
import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.logging.XLogger;
import java.util.ArrayList;
import java.util.List;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.SubscriberDetails;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.qos.SubscriberQosPolicy;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.userdata.UserDataService;

/**
 *
 *
 * <h1>Subscribe to topic
 *
 * <p>Receives all new discovered publications from {@link
 * EntityId.Predefined#ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR} reader and updates {@link
 * EntityId.Predefined#ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER} writer for those in which user
 * is interested in.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class TopicSubscriptionsManager extends SimpleSubscriber<RtpsTalkParameterListMessage> {

    private XLogger logger;
    private SedpDataFactory dataFactory;
    private List<Topic> topics = new ArrayList<>();
    private RtpsNetworkInterface networkIface;
    private StatefullReliableRtpsWriter<RtpsTalkParameterListMessage> subscriptionsWriter;
    private UserDataService userService;

    public TopicSubscriptionsManager(
            TracingToken tracingToken,
            RtpsTalkConfiguration config,
            RtpsNetworkInterface networkIface,
            StatefullReliableRtpsWriter<RtpsTalkParameterListMessage> subscriptionsWriter,
            UserDataService userService) {
        this.dataFactory = new SedpDataFactory(config);
        this.networkIface = networkIface;
        this.subscriptionsWriter = subscriptionsWriter;
        this.userService = userService;
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
    }

    public EntityId addSubscriber(SubscriberDetails subscriber) {
        var topicId = subscriber.topicId();
        logger.fine("Adding subscriber for topic id {0}", topicId);
        var topic = createTopicIfMissing(topicId);
        if (!topic.hasSubscribers()) {
            announceTopicSubscription(topicId, topic.getReaderEntityId(), subscriber.qosPolicy());
            topic.addListener(
                    subEvent -> {
                        logger.fine("New subscribe event for topic id {0}: {1}", topicId, subEvent);
                        userService.subscribeToRemoteWriter(
                                topic.getReaderEntityId(),
                                List.of(subEvent.writerUnicastLocator()),
                                subEvent.topicEndpointGuid(),
                                subEvent.subscriber());
                    });
        }
        topic.addSubscriber(subscriber);
        return topic.getReaderEntityId();
    }

    private Topic createTopicIfMissing(TopicId topicId) {
        var topic = topics.stream().filter(t -> t.isMatches(topicId)).findAny().orElse(null);
        if (topic == null) {
            var readers = networkIface.getOperatingEntities().getReaders();
            var readerEntityId =
                    readers.findEntityId(topicId)
                            .orElseGet(
                                    () ->
                                            readers.assignNewEntityId(
                                                    topicId, EntityKind.READER_NO_KEY));
            topic = new Topic(topicId, readerEntityId);
            topics.add(topic);
        }
        return topic;
    }

    /**
     * Receives messages from {@link
     * EntityId.Predefined#ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR} endpoint
     */
    @Override
    public void onNext(RtpsTalkParameterListMessage message) {
        var pl = message.parameterList();
        var pubTopic = (String) pl.getParameters().get(ParameterId.PID_TOPIC_NAME);
        Preconditions.notNull(pubTopic, "Received subscription without PID_TOPIC_NAME");
        var pubType = (String) pl.getParameters().get(ParameterId.PID_TYPE_NAME);
        Preconditions.notNull(pubType, "Received subscription without PID_TYPE_NAME");
        var participantGuid = (Guid) pl.getParameters().get(ParameterId.PID_PARTICIPANT_GUID);
        Preconditions.notNull(pubType, "Received subscription without PID_PARTICIPANT_GUID");
        var pubEndpointGuid = (Guid) pl.getParameters().get(ParameterId.PID_ENDPOINT_GUID);
        Preconditions.notNull(pubType, "Received subscription without PID_ENDPOINT_GUID");
        var pubUnicastLocator = (Locator) pl.getParameters().get(ParameterId.PID_UNICAST_LOCATOR);
        Preconditions.notNull(pubType, "Received subscription without PID_UNICAST_LOCATOR");
        Preconditions.equals(
                participantGuid.guidPrefix,
                pubEndpointGuid.guidPrefix,
                "Guid prefix missmatch for topic " + pubTopic);
        logger.fine(
                "Discovered publisher for topic {0} type {1} with endpoint {2}",
                pubTopic, pubType, pubEndpointGuid);
        var topicId = new TopicId(pubTopic, pubType);
        var topic = createTopicIfMissing(topicId);
        topic.addPublisher(pubUnicastLocator, pubEndpointGuid);
        subscription.request(1);
    }

    private void announceTopicSubscription(
            TopicId topicId, EntityId readerEntityId, SubscriberQosPolicy qosPolicy) {
        subscriptionsWriter.newChange(
                new RtpsTalkParameterListMessage(
                        dataFactory.createSubscriptionData(
                                topicId,
                                readerEntityId,
                                networkIface.getLocalDefaultUnicastLocator(),
                                qosPolicy)));
    }
}
