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
package pinorobotics.rtpstalk.impl.spec.discovery.sedp;

import id.xfunction.concurrent.flow.SimpleSubscriber;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SedpBuiltinSubscriptionsReader
        extends StatefullReliableRtpsReader<RtpsTalkParameterListMessage> {

    public SedpBuiltinSubscriptionsReader(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            OperatingEntities operatingEntities) {
        super(
                config,
                tracingToken,
                operatingEntities,
                EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR.getValue());
        subscribe(
                new SimpleSubscriber<RtpsTalkParameterListMessage>() {
                    @Override
                    public void onNext(RtpsTalkParameterListMessage message) {
                        processSubscription(message.parameterList());
                        subscription.request(1);
                    }
                });
    }

    private void processSubscription(ParameterList pl) {
        if (!isValid(pl)) return;
        Map<ParameterId, ?> params = pl.getParameters();
        var readerEndpointGuid = (Guid) params.get(ParameterId.PID_ENDPOINT_GUID);
        String topicName = (String) params.get(ParameterId.PID_TOPIC_NAME);
        String topicType = (String) params.get(ParameterId.PID_TYPE_NAME);
        Locator locator = (Locator) params.get(ParameterId.PID_UNICAST_LOCATOR);
        var topicId = new TopicId(topicName, topicType);
        logger.fine(
                "Remote participant {0} announced that it is subscribed to the topic {1} with"
                        + " its reader endpoint {2} and userdata unicast locator {3}",
                params.get(ParameterId.PID_PARTICIPANT_GUID), topicId, readerEndpointGuid, locator);
        getOperatingEntities()
                .getWriters()
                .findEntity(topicId)
                .ifPresentOrElse(
                        writer -> {
                            var unicast = List.of(locator);
                            try {
                                writer.matchedReaderAdd(readerEndpointGuid, unicast);
                            } catch (IOException e) {
                                logger.severe(e);
                            }
                        },
                        () ->
                                logger.fine(
                                        "There is no writer for such topic available, ignoring"
                                                + " subscription"));
    }

    private boolean isValid(ParameterList pl) {
        return pl.getParameters().containsKey(ParameterId.PID_PARTICIPANT_GUID)
                && pl.getParameters().containsKey(ParameterId.PID_TOPIC_NAME)
                && pl.getParameters().containsKey(ParameterId.PID_ENDPOINT_GUID)
                && pl.getParameters().containsKey(ParameterId.PID_UNICAST_LOCATOR);
    }
}
