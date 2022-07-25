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

import id.xfunction.logging.TracingToken;
import java.util.List;
import java.util.function.Consumer;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.SubscriberDetails;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.userdata.UserDataService;

/**
 * Receives all new discovered publications from the local {@link
 * EntityId.Predefined#ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR} reader and maintains a list of
 * all topics to which local participant can be subscribed or willing to subscribe (if publisher is
 * not yet available). When user effectively subscribes to any of them it updates {@link
 * EntityId.Predefined#ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER} writer and announces the
 * subscription to all remote participants.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class TopicSubscriptionsManager extends AbstractTopicManager<SubscriberDetails> {

    private SedpDataFactory dataFactory;
    private RtpsNetworkInterface networkIface;
    private UserDataService userService;

    public TopicSubscriptionsManager(
            TracingToken tracingToken,
            RtpsTalkConfiguration config,
            RtpsNetworkInterface networkIface,
            StatefullReliableRtpsWriter<RtpsTalkParameterListMessage> subscriptionsWriter,
            UserDataService userService) {
        super(tracingToken, subscriptionsWriter, ActorDetails.Type.Subscriber);
        this.dataFactory = new SedpDataFactory(config);
        this.networkIface = networkIface;
        this.userService = userService;
    }

    @Override
    protected Consumer<TopicMatchEvent<SubscriberDetails>> createListener(
            Topic<SubscriberDetails> topic) {
        return subEvent -> {
            logger.fine(
                    "New subscribe event for topic id" + " {0}: {1}", topic.getTopicId(), subEvent);
            userService.subscribeToRemoteWriter(
                    topic.getLocalTopicEntityId(),
                    List.of(subEvent.remoteUnicastLocator()),
                    subEvent.remoteEndpointGuid(),
                    subEvent.localActor().subscriber());
        };
    }

    @Override
    protected Topic<SubscriberDetails> createTopic(TopicId topicId) {
        var readers = networkIface.getOperatingEntities().getReaders();
        var readerEntityId = readers.assignEntityIdIfAbsent(topicId, EntityKind.READER_NO_KEY);
        return new Topic<>(topicId, readerEntityId);
    }

    @Override
    protected ParameterList createAnnouncementData(
            SubscriberDetails actor, Topic<SubscriberDetails> topic) {
        return dataFactory.createSubscriptionData(
                topic.getTopicId(),
                topic.getLocalTopicEntityId(),
                networkIface.getLocalDefaultUnicastLocator(),
                actor.qosPolicy());
    }
}
