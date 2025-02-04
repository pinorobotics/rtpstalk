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
package pinorobotics.rtpstalk.impl.spec.discovery.sedp;

import id.xfunction.logging.TracingToken;
import java.util.concurrent.Executor;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.DdsSpecReference;
import pinorobotics.rtpstalk.impl.spec.DdsVersion;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.KeyHash;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SedpBuiltinSubscriptionsReader
        extends StatefullReliableRtpsReader<RtpsTalkParameterListMessage> {

    @RtpsSpecReference(
            paragraph = "8.5.4.2",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "The SEDP maps the DDS built-in Entities for the DCPSSubscription,"
                            + " DCPSPublication, and DCPSTopic Topics.")
    @DdsSpecReference(paragraph = "2.2.5", protocolVersion = DdsVersion.DDS_1_4)
    private static final ReaderQosPolicySet DEFAULT_POLICY =
            new ReaderQosPolicySet(
                    ReliabilityQosPolicy.Kind.RELIABLE,
                    DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS);

    private LocalOperatingEntities operatingEntities;

    public SedpBuiltinSubscriptionsReader(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            LocalOperatingEntities operatingEntities,
            DataChannelFactory dataChannelFactory) {
        super(
                config,
                tracingToken,
                RtpsTalkParameterListMessage.class,
                publisherExecutor,
                operatingEntities,
                EntityId.Predefined.ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR.getValue(),
                DEFAULT_POLICY,
                dataChannelFactory);
        this.operatingEntities = operatingEntities;
    }

    @Override
    protected void processInlineQos(
            Guid writer,
            SequenceNumber seqNum,
            RtpsTalkParameterListMessage message,
            ParameterList inlineQos) {
        var inlineQosParams = inlineQos.getProtocolParameters();
        if (inlineQosParams.isEmpty()) return;
        logger.fine("Processing inlineQos");
        if (!inlineQosParams.hasDisposedObjects()) return;
        inlineQosParams
                .getFirstParameter(ParameterId.PID_KEY_HASH, KeyHash.class)
                .map(KeyHash::asGuid)
                .or(
                        () ->
                                message.parameterList()
                                        .map(ParameterList::getProtocolParameters)
                                        .flatMap(
                                                params ->
                                                        params.getFirstParameter(
                                                                ParameterId.PID_ENDPOINT_GUID,
                                                                Guid.class)))
                .ifPresent(this::removeMatchedReader);
    }

    private void removeMatchedReader(Guid readerGuid) {
        logger.fine("Matched reader {0} marked as disposed", readerGuid);
        boolean isRemoved = false;
        for (var writer : operatingEntities.getLocalWriters().getEntities()) {
            if (writer.matchedReaderLookup(readerGuid).isPresent()) {
                writer.matchedReaderRemove(readerGuid);
                isRemoved = true;
                break;
            }
        }
        if (!isRemoved) {
            logger.fine(
                    "Reader {0} does not match any of the available writers, ignoring it...",
                    readerGuid);
        }
    }
}
