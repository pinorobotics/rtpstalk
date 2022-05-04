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
package pinorobotics.rtpstalk.discovery.sedp;

import id.xfunction.concurrent.flow.SimpleSubscriber;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.OperatingEntities;
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class SedpBuiltinSubscriptionsReader extends StatefullRtpsReader<ParameterList> {

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
                new SimpleSubscriber<ParameterList>() {
                    @Override
                    public void onNext(ParameterList pl) {
                        processSubscription(pl);
                        subscription.request(1);
                    }
                });
    }

    private void processSubscription(ParameterList pl) {
        if (!isValid(pl)) return;
        Map<ParameterId, ?> params = pl.params;
        var readerEndpointGuid = (Guid) params.get(ParameterId.PID_ENDPOINT_GUID);
        String topicName = (String) params.get(ParameterId.PID_TOPIC_NAME);
        logger.fine(
                "Remote participant {0} announced that it is subscribed to the topic {1} with"
                        + " endpoint {2}",
                params.get(ParameterId.PID_PARTICIPANT_GUID), topicName, readerEndpointGuid);
        getOperatingEntities()
                .findWriter(topicName)
                .ifPresent(
                        writer -> {
                            if (params.get(ParameterId.PID_UNICAST_LOCATOR)
                                    instanceof Locator locator) {
                                var unicast = List.of(locator);
                                try {
                                    writer.matchedReaderAdd(readerEndpointGuid, unicast);
                                } catch (IOException e) {
                                    logger.severe(e);
                                }
                            }
                        });
    }

    private boolean isValid(ParameterList pl) {
        return pl.params.containsKey(ParameterId.PID_PARTICIPANT_GUID)
                && pl.params.containsKey(ParameterId.PID_TOPIC_NAME)
                && pl.params.containsKey(ParameterId.PID_ENDPOINT_GUID)
                && pl.params.containsKey(ParameterId.PID_UNICAST_LOCATOR);
    }
}
