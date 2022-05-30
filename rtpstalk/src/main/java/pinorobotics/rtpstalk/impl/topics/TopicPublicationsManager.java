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
import id.xfunction.XObservable;
import id.xfunction.logging.XLogger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Flow.Publisher;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.writer.StatefullRtpsWriter;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.DestinationOrderQosPolicy;
import pinorobotics.rtpstalk.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.messages.Duration;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.userdata.UserDataService;

/** @author lambdaprime intid@protonmail.com */
public class TopicPublicationsManager extends XObservable<SubscribeEvent> {

    private XLogger logger;
    private RtpsTalkConfiguration config;
    private StatefullRtpsWriter<ParameterList> publicationWriter;
    private RtpsNetworkInterface networkIface;
    private UserDataService userService;

    public TopicPublicationsManager(
            TracingToken tracingToken,
            RtpsTalkConfiguration config,
            RtpsNetworkInterface networkIface,
            StatefullRtpsWriter<ParameterList> publicationWriter,
            UserDataService userService) {
        this.config = config;
        this.networkIface = networkIface;
        this.publicationWriter = publicationWriter;
        this.userService = userService;
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
    }

    public void addPublisher(TopicId topicId, Publisher<RawData> publisher) {
        logger.fine("Adding new publisher for topic id {0}", topicId);
        var operatingEntities = networkIface.getOperatingEntities();
        var writers = operatingEntities.getWriters();
        Preconditions.isTrue(
                !writers.findEntityId(topicId).isPresent(),
                "Writer for topic " + topicId + " already present");
        EntityId writerEntityId = writers.assignNewEntityId(topicId, EntityKind.WRITER_NO_KEY);
        EntityId readerEntityId =
                operatingEntities.getReaders().assignNewEntityId(topicId, EntityKind.READER_NO_KEY);

        publicationWriter.newChange(
                createPublicationData(
                        topicId, writerEntityId, networkIface.getLocalDefaultUnicastLocator()));

        userService.publish(writerEntityId, readerEntityId, publisher);
    }

    private ParameterList createPublicationData(
            TopicId topicId, EntityId entityId, Locator defaultUnicastLocator) {
        var guid = new Guid(config.guidPrefix(), entityId);
        var params =
                List.<Entry<ParameterId, Object>>of(
                        Map.entry(ParameterId.PID_UNICAST_LOCATOR, defaultUnicastLocator),
                        Map.entry(
                                ParameterId.PID_PARTICIPANT_GUID, config.getLocalParticipantGuid()),
                        Map.entry(ParameterId.PID_TOPIC_NAME, topicId.name()),
                        Map.entry(ParameterId.PID_TYPE_NAME, topicId.type()),
                        Map.entry(ParameterId.PID_ENDPOINT_GUID, guid),
                        Map.entry(ParameterId.PID_KEY_HASH, guid),
                        Map.entry(
                                ParameterId.PID_PROTOCOL_VERSION,
                                ProtocolVersion.Predefined.Version_2_3.getValue()),
                        Map.entry(
                                ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue()),
                        Map.entry(
                                ParameterId.PID_DURABILITY,
                                new DurabilityQosPolicy(
                                        DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS)),
                        Map.entry(
                                ParameterId.PID_RELIABILITY,
                                new ReliabilityQosPolicy(
                                        ReliabilityQosPolicy.Kind.RELIABLE,
                                        Duration.Predefined.ZERO.getValue())),
                        Map.entry(
                                ParameterId.PID_DESTINATION_ORDER,
                                new DestinationOrderQosPolicy(
                                        DestinationOrderQosPolicy.Kind
                                                .BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS)));
        return new ParameterList(params);
    }
}
