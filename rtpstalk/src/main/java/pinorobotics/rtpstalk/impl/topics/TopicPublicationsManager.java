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
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.function.Consumer;
import pinorobotics.rtpstalk.impl.PublisherDetails;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.userdata.UserDataService;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class TopicPublicationsManager extends AbstractTopicManager<PublisherDetails> {

    private XLogger logger;
    private SedpDataFactory dataFactory;
    private RtpsNetworkInterface networkIface;
    private UserDataService userService;
    private OperatingEntities operatingEntities;

    public TopicPublicationsManager(
            TracingToken tracingToken,
            RtpsTalkConfigurationInternal config,
            RtpsNetworkInterface networkIface,
            StatefullReliableRtpsWriter<RtpsTalkParameterListMessage> publicationWriter,
            UserDataService userService) {
        super(tracingToken, publicationWriter, ActorDetails.Type.Publisher);
        this.dataFactory = new SedpDataFactory(config);
        this.networkIface = networkIface;
        operatingEntities = networkIface.getOperatingEntities();
        this.userService = userService;
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    @Override
    public EntityId addLocalActor(PublisherDetails actor) {
        var topicId = actor.topicId();
        Preconditions.isTrue(
                !findTopicById(topicId).map(Topic::hasLocalActors).orElse(false),
                "Only one local writer per topic " + topicId + " allowed");
        var topic = createTopic(topicId);
        EntityId readerEntityId =
                operatingEntities.getReaders().assignNewEntityId(topicId, EntityKind.READER_NO_KEY);
        // until user publisher is registered it may discard any submitted messages
        // to avoid losing them we register publisher here and not during match event
        userService.publish(topic.getLocalTopicEntityId(), readerEntityId, actor.publisher());
        var writerEntityId = super.addLocalActor(actor);
        Preconditions.equals(
                writerEntityId,
                topic.getLocalTopicEntityId(),
                "Same local topic with different entity ids");
        return writerEntityId;
    }

    @Override
    protected Consumer<TopicMatchEvent<PublisherDetails>> createListener(
            Topic<PublisherDetails> topic) {
        return subEvent -> {
            var topicId = topic.getTopicId();
            var remoteActor = subEvent.remoteActor();
            logger.fine(
                    "New match event between local publisher and remote subscriber {0} for topic id"
                            + " {1}",
                    remoteActor, topicId);
            var writer = operatingEntities.getWriters().findEntity(topicId).orElse(null);
            if (writer == null) {
                EntityId readerEntityId =
                        operatingEntities
                                .getReaders()
                                .assignNewEntityId(topicId, EntityKind.READER_NO_KEY);
                userService.publish(
                        topic.getLocalTopicEntityId(),
                        readerEntityId,
                        subEvent.localActor().publisher());
                writer =
                        operatingEntities
                                .getWriters()
                                .findEntity(topic.getTopicId())
                                .orElseThrow(
                                        () ->
                                                new RuntimeException(
                                                        "Could not register local data writer for"
                                                                + " topic "
                                                                + topicId));
            }
            try {
                writer.matchedReaderAdd(
                        remoteActor.endpointGuid(), remoteActor.writerUnicastLocator());
            } catch (IOException e) {
                logger.severe(e);
            }
        };
    }

    @Override
    protected Topic<PublisherDetails> createTopic(TopicId topicId) {
        EntityId writerEntityId =
                operatingEntities.getWriters().assignNewEntityId(topicId, EntityKind.WRITER_NO_KEY);
        return new Topic<>(topicId, writerEntityId);
    }

    @Override
    protected ParameterList createAnnouncementData(
            PublisherDetails actor, Topic<PublisherDetails> topic) {
        return dataFactory.createPublicationData(
                topic.getTopicId(),
                topic.getLocalTopicEntityId(),
                networkIface.getLocalDefaultUnicastLocator(),
                actor.qosPolicy());
    }
}
