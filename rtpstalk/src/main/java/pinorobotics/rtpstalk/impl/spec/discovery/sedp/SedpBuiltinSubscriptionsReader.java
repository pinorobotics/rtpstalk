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

import id.xfunction.logging.TracingToken;
import java.util.Map;
import java.util.concurrent.Executor;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.KeyHash;
import pinorobotics.rtpstalk.impl.spec.messages.StatusInfo;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SedpBuiltinSubscriptionsReader
        extends StatefullReliableRtpsReader<RtpsTalkParameterListMessage> {
    private OperatingEntities operatingEntities;

    public SedpBuiltinSubscriptionsReader(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            OperatingEntities operatingEntities) {
        super(
                config,
                tracingToken,
                RtpsTalkParameterListMessage.class,
                publisherExecutor,
                operatingEntities,
                EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR.getValue());
        this.operatingEntities = operatingEntities;
    }

    @Override
    protected void processInlineQos(Guid writer, ParameterList inlineQos) {
        var params = inlineQos.getParameters();
        if (params.isEmpty()) return;
        logger.fine("Processing inlineQos");
        processStatusInfo(writer, params);
    }

    private void processStatusInfo(Guid writerGuid, Map<ParameterId, ?> params) {
        if (params.get(ParameterId.PID_STATUS_INFO) instanceof StatusInfo info) {
            if (info.isDisposed()) {
                if (params.get(ParameterId.PID_KEY_HASH) instanceof KeyHash keyHash) {
                    var readerGuid = keyHash.asGuid();
                    logger.fine("Reader {0} marked subscription as disposed", readerGuid);
                    boolean isRemoved = false;
                    for (var writer : operatingEntities.getWriters().getEntities()) {
                        if (writer.matchedReaderLookup(readerGuid).isPresent()) {
                            writer.matchedReaderRemove(readerGuid);
                            isRemoved = true;
                            break;
                        }
                    }
                    if (!isRemoved) {
                        logger.fine(
                                "Reader {0} does not match any of the available writers, ignoring"
                                        + " it...",
                                readerGuid);
                    }
                }
            }
        }
    }
}
