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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.writer.StatefullRtpsWriter;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.utils.GuidUtils;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.userdata.UserDataService;

/**
 * Receives all new discovered publications from {@link
 * EntityId.Predefined#ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR} reader and updates {@link
 * EntityId.Predefined#ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER} writer for those in which user
 * is interested in.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class TopicPublicationsManager extends SimpleSubscriber<ParameterList> {

    private XLogger logger;
    private List<Topic> topics = new ArrayList<>();
    private RtpsTalkConfiguration config;
    private RtpsNetworkInterface networkIface;
    private StatefullRtpsWriter<ParameterList> subscriptionsWriter;
    private UserDataService userService;
    private GuidUtils guidUtils = new GuidUtils();

    public TopicPublicationsManager(
            TracingToken tracingToken,
            RtpsTalkConfiguration config,
            RtpsNetworkInterface networkIface,
            StatefullRtpsWriter<ParameterList> subscriptionsWriter,
            UserDataService userService) {
        this.config = config;
        this.networkIface = networkIface;
        this.subscriptionsWriter = subscriptionsWriter;
        this.userService = userService;
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
    }

    public void addSubscriber(String topicName, String topicType, Subscriber<RawData> subscriber) {
        logger.fine("Adding subscriber for topic {0} type {1}", topicName, topicType);
        var topic = createTopicIfMissing(topicName, topicType);
        topic.addSubscriber(subscriber);
    }

    private Topic createTopicIfMissing(String topicName, String topicType) {
        var topic =
                topics.stream()
                        .filter(t -> t.isMatches(topicName, topicType))
                        .findAny()
                        .orElse(null);
        if (topic == null) {
            topic = newTopic(topicName, topicType);
            topics.add(topic);
        }
        return topic;
    }

    /**
     * Receives messages from {@link
     * EntityId.Predefined#ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR} endpoint
     */
    @Override
    public void onNext(ParameterList pl) {
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
        var topic = createTopicIfMissing(pubTopic, pubType);
        topic.addPublisher(pubUnicastLocator, pubEndpointGuid);
        subscription.request(1);
    }

    private Topic newTopic(String topicName, String topicType) {
        var topic = new Topic(topicName, topicType);
        topic.addListener(
                subEvent -> {
                    logger.fine("New subscribe event {0}", subEvent);
                    subscriptionsWriter.newChange(
                            createSubscriptionData(
                                    topicName,
                                    topicType,
                                    guidUtils.readerEntityId(subEvent.topicEndpointGuid()),
                                    networkIface.getLocalDefaultUnicastLocator()));
                    userService.subscribe(
                            List.of(subEvent.writerUnicastLocator()),
                            subEvent.topicEndpointGuid(),
                            subEvent.subscriber());
                });
        return topic;
    }

    private ParameterList createSubscriptionData(
            String topicName, String typeName, EntityId entityId, Locator defaultUnicastLocator) {
        var params =
                List.<Entry<ParameterId, Object>>of(
                        Map.entry(ParameterId.PID_UNICAST_LOCATOR, defaultUnicastLocator),
                        Map.entry(
                                ParameterId.PID_PARTICIPANT_GUID, config.getLocalParticipantGuid()),
                        Map.entry(ParameterId.PID_TOPIC_NAME, topicName),
                        Map.entry(ParameterId.PID_TYPE_NAME, typeName),
                        Map.entry(
                                ParameterId.PID_ENDPOINT_GUID,
                                new Guid(config.guidPrefix(), entityId)),
                        Map.entry(
                                ParameterId.PID_PROTOCOL_VERSION,
                                ProtocolVersion.Predefined.Version_2_3.getValue()),
                        Map.entry(
                                ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue()));
        return new ParameterList(params);
    }
}
