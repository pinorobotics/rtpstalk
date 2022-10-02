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
import id.xfunction.lang.XThread;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import pinorobotics.rtpstalk.impl.PublisherDetails;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
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
    private TracingToken tracingToken;
    private Map<TopicId, Long> announcementSeqNums = new HashMap<>();
    private RtpsTalkConfigurationInternal config;

    public TopicPublicationsManager(
            TracingToken tracingToken,
            RtpsTalkConfigurationInternal config,
            RtpsNetworkInterface networkIface,
            StatefullReliableRtpsWriter<RtpsTalkParameterListMessage> publicationWriter,
            UserDataService userService) {
        super(tracingToken, publicationWriter, ActorDetails.Type.Publisher);
        this.tracingToken = tracingToken;
        this.config = config;
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
        userService.publish(topic.getLocalTopicEntityId(), readerEntityId, actor);
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
            var writer = getOrCreateWriter(topic, subEvent.localActor());
            waitForTopicBeAnnouncedToReader(
                            remoteActor.endpointGuid().guidPrefix, topic.getTopicId())
                    .thenRun(
                            () -> {
                                try {
                                    writer.matchedReaderAdd(
                                            remoteActor.endpointGuid(),
                                            remoteActor.writerUnicastLocator(),
                                            remoteActor.reliabilityKind());
                                } catch (IOException e) {
                                    logger.severe(e);
                                }
                            });
        };
    }

    private StatefullReliableRtpsWriter<?> getOrCreateWriter(
            Topic<PublisherDetails> topic, PublisherDetails publisherDetails) {
        var topicId = topic.getTopicId();
        return operatingEntities
                .getWriters()
                .findEntity(topicId)
                .or(
                        () -> {
                            EntityId readerEntityId =
                                    operatingEntities
                                            .getReaders()
                                            .assignNewEntityId(topicId, EntityKind.READER_NO_KEY);
                            userService.publish(
                                    topic.getLocalTopicEntityId(),
                                    readerEntityId,
                                    publisherDetails);
                            return operatingEntities.getWriters().findEntity(topicId);
                        })
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "Could not register local data writer for"
                                                + " topic "
                                                + topic));
    }

    /**
     * We want to avoid publishing changes to the Reader when it did not yet received an
     * announcement about the topic from SEDP.
     */
    private CompletionStage<Void> waitForTopicBeAnnouncedToReader(
            GuidPrefix guidPrefix, TopicId topic) {
        var future = new CompletableFuture<Void>();
        var announcementSeqNum = announcementSeqNums.get(topic);
        if (announcementSeqNum == null) {
            future.completeExceptionally(
                    new RuntimeException(
                            "Cannot find topic " + topic + " announcement sequence number"));
            return future;
        }
        var remoteReaderGuid =
                new Guid(
                        guidPrefix,
                        EntityId.Predefined.ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR);
        var sedpPublicationsDetectorProxy =
                announcementsWriter.matchedReaderLookup(remoteReaderGuid).orElse(null);
        if (sedpPublicationsDetectorProxy == null) {
            future.completeExceptionally(
                    new RuntimeException("Reader " + remoteReaderGuid + " is not known to SEDP"));
            return future;
        }
        new Thread(tracingToken.toString()) {
            @Override
            public void run() {
                int retries = 5;
                while (sedpPublicationsDetectorProxy.getHighestAckedSeqNum() < announcementSeqNum) {
                    logger.fine(
                            "Waiting for topic {0} to be annouced to the reader {1}, current retry"
                                    + " {2}",
                            topic, remoteReaderGuid, retries);
                    XThread.sleep(config.publicConfig().heartbeatPeriod().toMillis());
                    retries--;
                    if (retries < 0) {
                        future.completeExceptionally(
                                new RuntimeException(
                                        "Topic "
                                                + topic
                                                + " was not announced to the Reader "
                                                + remoteReaderGuid
                                                + " in time"));
                        break;
                    }
                }
                future.complete(null);
            }
        }.start();
        return future;
    }

    @Override
    protected long announceTopicInterest(PublisherDetails actor, Topic<PublisherDetails> topic) {
        var seqNum = super.announceTopicInterest(actor, topic);
        announcementSeqNums.put(topic.getTopicId(), seqNum);
        return seqNum;
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
