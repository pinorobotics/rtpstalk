/*
 * Copyright 2026 pinorobotics
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
package pinorobotics.rtpstalk.impl.actors;

import id.xfunction.Preconditions;
import id.xfunction.concurrent.flow.TransformSubscriber;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import id.xfunction.util.ImmutableMultiMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.exceptions.RtpsTalkException;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.messages.ProtocolParameterMap;
import pinorobotics.rtpstalk.impl.spec.behavior.ParticipantsRegistry;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.RtpsReader;
import pinorobotics.rtpstalk.impl.spec.discovery.sedp.SedpBuiltinPublicationsReader;
import pinorobotics.rtpstalk.impl.spec.discovery.sedp.SedpBuiltinSubscriptionsReader;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;

/**
 * Receive {@link RtpsTalkParameterListMessage} from SEDP {@link RtpsReader} (mainly from {@link
 * SedpBuiltinPublicationsReader} or {@link SedpBuiltinSubscriptionsReader}) and extract {@link
 * RemoteActorDetails} from them
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class TopicDiscoveryPublisher implements Publisher<RemoteActorDetails> {
    private XLogger logger;
    private ParticipantsRegistry participantsRegistry;
    private Publisher<RtpsTalkParameterListMessage> targetPublisher;

    public TopicDiscoveryPublisher(
            TracingToken tracingToken,
            ParticipantsRegistry participantsRegistry,
            Publisher<RtpsTalkParameterListMessage> targetPublisher) {
        this.participantsRegistry = participantsRegistry;
        this.targetPublisher = targetPublisher;
        this.logger = XLogger.getLogger(getClass(), tracingToken);
    }

    @Override
    public void subscribe(Subscriber<? super RemoteActorDetails> subscriber) {
        var transformer = new TransformSubscriber<>(subscriber, this::transform);
        targetPublisher.subscribe(transformer);
    }

    public Optional<RemoteActorDetails> transform(RtpsTalkParameterListMessage message) {
        ProtocolParameterMap pm =
                message.parameterList().map(ParameterList::getProtocolParameters).orElse(null);
        if (pm == null) {
            logger.warning(
                    "No RTPS protocol parameters inside RTPS parameter list message, ignoring it");
            return Optional.empty();
        }
        if (!isValid(pm)) {
            logger.warning("Non valid publications data received, ignoring it");
            return Optional.empty();
        }
        var pubTopic =
                pm.getFirstParameter(ParameterId.PID_TOPIC_NAME, String.class)
                        .orElseThrow(
                                () ->
                                        new RtpsTalkException(
                                                "Received subscription without"
                                                        + " PID_TOPIC_NAME"));
        var pubType =
                pm.getFirstParameter(ParameterId.PID_TYPE_NAME, String.class)
                        .orElseThrow(
                                () ->
                                        new RtpsTalkException(
                                                "Received subscription without PID_TYPE_NAME"));
        var topicId = new TopicId(pubTopic, pubType);
        var pubEndpointGuid =
                pm.getFirstParameter(ParameterId.PID_ENDPOINT_GUID, Guid.class)
                        .orElseThrow(
                                () ->
                                        new RtpsTalkException(
                                                "Received subscription without"
                                                        + " PID_ENDPOINT_GUID"));

        if (!isEndpointSupported(topicId, pubEndpointGuid)) {
            logger.warning(
                    "Endpoint Guid {0} for topic {1} is not supported", pubEndpointGuid, topicId);
            return Optional.empty();
        }

        var participantGuid =
                (Guid)
                        findParticipantInfo(
                                        pubEndpointGuid.guidPrefix,
                                        pm,
                                        ParameterId.PID_PARTICIPANT_GUID)
                                .stream()
                                .findFirst()
                                .orElse(null);
        if (participantGuid == null) {
            logger.warning(
                    "Could not find participant for endpoint Guid {0}, ignoring subscription"
                            + " for topic {1}",
                    pubEndpointGuid, topicId);
            return Optional.empty();
        }

        var pubUnicastLocators =
                (List<Locator>)
                        findParticipantInfo(
                                pubEndpointGuid.guidPrefix,
                                pm,
                                ParameterId.PID_DEFAULT_UNICAST_LOCATOR);
        if (pubUnicastLocators.isEmpty()) {
            logger.warning(
                    "Could not find locator for endpoint Guid {0}, ignoring subscription for"
                            + " topic {1}",
                    pubEndpointGuid, topicId);
            return Optional.empty();
        }

        Preconditions.equals(
                participantGuid.guidPrefix,
                pubEndpointGuid.guidPrefix,
                "Guid prefix missmatch for topic %s",
                pubTopic);
        var reliabilityKind =
                pm.getReliabilityKind()
                        .orElseGet(
                                () -> {
                                    logger.warning(
                                            "Received subscription without"
                                                    + " ReliabilityQosPolicy, assuming BEST_EFFORT"
                                                    + " by default...");
                                    return ReliabilityQosPolicy.Kind.BEST_EFFORT;
                                });
        var durabilityKind =
                pm.getDurabilityKind()
                        .orElseGet(
                                () -> {
                                    logger.warning(
                                            "Received subscription without DurabilityQosPolicy,"
                                                    + " assuming VOLATILE_DURABILITY_QOS by"
                                                    + " default...");
                                    return DurabilityQosPolicy.Kind.VOLATILE_DURABILITY_QOS;
                                });
        var remoteActorDetails =
                new RemoteActorDetails(
                        topicId,
                        pubEndpointGuid,
                        pubUnicastLocators,
                        reliabilityKind,
                        durabilityKind);
        logger.info("Discovered remote actor {0}", remoteActorDetails);
        return Optional.of(remoteActorDetails);
    }

    private List<?> findParticipantInfo(
            GuidPrefix guidPrefix,
            ImmutableMultiMap<ParameterId, Object> params,
            ParameterId parameterId) {
        var participantInfo = params.get(parameterId);
        if (!participantInfo.isEmpty()) return participantInfo;
        logger.fine(
                "Received subscription without {0}, trying to find it in Participants Registry by"
                        + " guid prefix {1}",
                parameterId, guidPrefix);
        var pl = participantsRegistry.getSpdpDiscoveredParticipantData(guidPrefix).orElse(null);
        if (pl == null) {
            logger.fine(
                    "Could not find participant with guid prefix {0} in Participants Registry",
                    guidPrefix);
            return List.of();
        }
        return pl.getProtocolParameters().get(parameterId);
    }

    private boolean isEndpointSupported(TopicId topicId, Guid pubEndpointGuid) {
        boolean isSupported = true;
        if (pubEndpointGuid.entityId.entityKind() == EntityKind.READER.getValue())
            isSupported = false;
        if (pubEndpointGuid.entityId.entityKind() == EntityKind.WRITER.getValue())
            isSupported = false;
        if (!isSupported) {
            logger.warning(
                    "Remote participant for topic {0} is using keyed endpoint and will be ignored."
                        + " Only NOKEY readers and writers are supported. Check that there is no"
                        + " fields in IDL files annotated with @key.",
                    topicId);
        }
        return isSupported;
    }

    private boolean isValid(ProtocolParameterMap pl) {
        return pl.containsKey(ParameterId.PID_TOPIC_NAME)
                && pl.containsKey(ParameterId.PID_TYPE_NAME)
                && pl.containsKey(ParameterId.PID_ENDPOINT_GUID);
    }
}
