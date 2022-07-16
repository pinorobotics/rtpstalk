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
import java.util.Optional;
import java.util.function.Consumer;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.topics.ActorDetails.Type;

/**
 * @author lambdaprime intid@protonmail.com
 */
public abstract class AbstractTopicManager<A extends ActorDetails>
        extends SimpleSubscriber<RtpsTalkParameterListMessage> {

    protected XLogger logger;
    private List<Topic<A>> topics = new ArrayList<>();
    private StatefullReliableRtpsWriter<RtpsTalkParameterListMessage> announcementsWriter;
    private ActorDetails.Type actorsType;

    public AbstractTopicManager(
            TracingToken tracingToken,
            StatefullReliableRtpsWriter<RtpsTalkParameterListMessage> subscriptionsWriter,
            ActorDetails.Type actorsType) {
        this.announcementsWriter = subscriptionsWriter;
        this.actorsType = actorsType;
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
    }

    public EntityId addLocalActor(A actor) {
        var topicId = actor.topicId();
        logger.fine("Adding {0} for topic id {1}", actorsType, topicId);
        var topic = createTopicIfMissing(topicId);
        if (!topic.hasLocalActors()) {
            announceTopicInterest(actor, topic);
            topic.addListener(createListener(topic));
        }
        topic.addLocalActor(actor);
        return topic.getLocalTopicEntityId();
    }

    protected abstract Consumer<TopicMatchEvent<A>> createListener(Topic<A> topic);

    private Topic<A> createTopicIfMissing(TopicId topicId) {
        var topic = findTopicById(topicId).orElse(null);
        if (topic == null) {
            topic = createTopic(topicId);
            topics.add(topic);
        }
        return topic;
    }

    protected Optional<Topic<A>> findTopicById(TopicId topicId) {
        return topics.stream().filter(t -> t.isMatches(topicId)).findAny();
    }

    protected abstract Topic<A> createTopic(TopicId topicId);

    /**
     * Receives messages from {@link
     * EntityId.Predefined#ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR} endpoint when new publishers
     * discovered
     */
    @Override
    public void onNext(RtpsTalkParameterListMessage message) {
        var pl = message.parameterList();
        if (!isValid(pl)) return;
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
                "Discovered {0} for topic {1} type {2} with endpoint {3}",
                actorsType == Type.Publisher ? Type.Subscriber : Type.Publisher,
                pubTopic,
                pubType,
                pubEndpointGuid);
        var topicId = new TopicId(pubTopic, pubType);
        var topic = createTopicIfMissing(topicId);
        topic.addRemoteActor(pubUnicastLocator, pubEndpointGuid);
        subscription.request(1);
    }

    protected abstract ParameterList createAnnouncementData(A actor, Topic<A> topic);

    private void announceTopicInterest(A actor, Topic<A> topic) {
        announcementsWriter.newChange(
                new RtpsTalkParameterListMessage(createAnnouncementData(actor, topic)));
    }

    private boolean isValid(ParameterList pl) {
        return pl.getParameters().containsKey(ParameterId.PID_PARTICIPANT_GUID)
                && pl.getParameters().containsKey(ParameterId.PID_TOPIC_NAME)
                && pl.getParameters().containsKey(ParameterId.PID_ENDPOINT_GUID)
                && pl.getParameters().containsKey(ParameterId.PID_UNICAST_LOCATOR);
    }
}