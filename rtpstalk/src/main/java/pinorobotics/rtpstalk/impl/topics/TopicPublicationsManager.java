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
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.PublisherDetails;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.userdata.UserDataService;

/** @author lambdaprime intid@protonmail.com */
public class TopicPublicationsManager extends XObservable<SubscribeEvent> {

    private XLogger logger;
    private SedpDataFactory dataFactory;
    private StatefullReliableRtpsWriter<ParameterList> publicationWriter;
    private RtpsNetworkInterface networkIface;
    private UserDataService userService;

    public TopicPublicationsManager(
            TracingToken tracingToken,
            RtpsTalkConfiguration config,
            RtpsNetworkInterface networkIface,
            StatefullReliableRtpsWriter<ParameterList> publicationWriter,
            UserDataService userService) {
        this.dataFactory = new SedpDataFactory(config);
        this.networkIface = networkIface;
        this.publicationWriter = publicationWriter;
        this.userService = userService;
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
    }

    public void addPublisher(PublisherDetails publisher) {
        var topicId = publisher.topicId();
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
                dataFactory.createPublicationData(
                        topicId,
                        writerEntityId,
                        networkIface.getLocalDefaultUnicastLocator(),
                        publisher.qosPolicy()));

        userService.publish(writerEntityId, readerEntityId, publisher.publisher());
    }
}
