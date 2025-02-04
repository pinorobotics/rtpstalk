/*
 * Copyright 2022 pinorobotics
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
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.discovery.sedp.SedpBuiltinSubscriptionsReader;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.userdata.UserDataService;

/**
 * Subscribes to local {@link SedpBuiltinSubscriptionsReader} which receives messages from all
 * discovered remote {@link
 * BuiltinEndpointSet.Endpoint#DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_ANNOUNCER}
 *
 * @author lambdaprime intid@protonmail.com
 */
public class LocalTopicPublicationsManager extends AbstractTopicManager<PublisherDetails> {

    private XLogger logger;
    private SedpDataFactory dataFactory;
    private RtpsNetworkInterface networkIface;
    private UserDataService userService;
    private LocalOperatingEntities operatingEntities;
    private TracingToken tracingToken;
    private Map<TopicId, Long> announcementSeqNums = new HashMap<>();
    private RtpsTalkConfigurationInternal config;

    public LocalTopicPublicationsManager(
            TracingToken tracingToken,
            RtpsTalkConfigurationInternal config,
            RtpsNetworkInterface networkIface,
            StatefullReliableRtpsWriter<RtpsTalkParameterListMessage> publicationWriter,
            UserDataService userService) {
        super(
                tracingToken,
                publicationWriter,
                networkIface.getParticipantsRegistry(),
                ActorDetails.Type.Publisher);
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
                "Only one local writer per topic %s is allowed",
                topicId);
        var topic = createTopic(topicId);
        // until user publisher is registered it may discard any submitted messages
        // to avoid losing them we register publisher here and not during match event
        userService.publish(topic.getLocalTopicEntityId(), actor);
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
                                            new ReaderQosPolicySet(
                                                    remoteActor.reliabilityKind(),
                                                    remoteActor.durabilityKind()));
                                } catch (IOException e) {
                                    logger.severe(e);
                                }
                            })
                    .exceptionally(this::exceptionHandler);
        };
    }

    private StatefullReliableRtpsWriter<?> getOrCreateWriter(
            Topic<PublisherDetails> topic, PublisherDetails publisherDetails) {
        var topicId = topic.getTopicId();
        return operatingEntities
                .getLocalWriters()
                .findEntity(topicId)
                .or(
                        () -> {
                            userService.publish(topic.getLocalTopicEntityId(), publisherDetails);
                            return operatingEntities.getLocalWriters().findEntity(topicId);
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
        var future = new CompletableFuture<Void>().exceptionally(this::exceptionHandler);
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
                var hb = config.publicConfig().heartbeatPeriod();
                var maxRetries = config.publicConfig().readerAckTopicDuration().dividedBy(hb);
                var retriesLeft = maxRetries;
                while (sedpPublicationsDetectorProxy.getHighestAckedSeqNum() < announcementSeqNum) {
                    logger.fine(
                            "Waiting for topic {0} to be annouced to the reader {1}, current retry"
                                    + " {2}",
                            topic, remoteReaderGuid, retriesLeft);
                    XThread.sleep(hb.toMillis());
                    retriesLeft--;
                    if (retriesLeft < 0) {
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
                logger.fine(
                        "Topic {0} acked by the reader {1} with retry {2}",
                        topic, remoteReaderGuid, maxRetries - retriesLeft);
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
                operatingEntities
                        .getLocalWriters()
                        .assignNewEntityId(topicId, EntityKind.WRITER_NO_KEY);
        return new Topic<>(topicId, writerEntityId);
    }

    @Override
    protected ParameterList createAnnouncementData(
            PublisherDetails actor, Topic<PublisherDetails> topic) {
        return dataFactory.createDiscoveredWriterData(
                topic.getTopicId(),
                topic.getLocalTopicEntityId(),
                networkIface.getLocalDefaultUnicastLocator(),
                actor.qosPolicy());
    }

    private Void exceptionHandler(Throwable ex) {
        logger.severe(ex);
        return null;
    }
}
