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

import id.xfunction.Preconditions;
import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import java.io.IOException;
import java.util.List;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.messages.ProtocolParameterMap;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet.Endpoint;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet.EndpointType;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.metrics.RtpsTalkMetrics;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
@RtpsSpecReference(
        paragraph = "8.5.5.1",
        protocolVersion = Predefined.Version_2_3,
        text = "Discovery of a new remote Participant")
public class SedpBuiltinEndpointsConfigurator extends SimpleSubscriber<RtpsTalkParameterListMessage>
        implements AutoCloseable {
    private final Meter METER =
            GlobalOpenTelemetry.getMeter(SedpBuiltinEndpointsConfigurator.class.getSimpleName());
    private final LongCounter PARTICIPANTS_COUNT_METER =
            METER.counterBuilder(RtpsTalkMetrics.PARTICIPANTS_COUNT_METRIC)
                    .setDescription(RtpsTalkMetrics.PARTICIPANTS_COUNT_METRIC_DESCRIPTION)
                    .build();
    private SedpBuiltinSubscriptionsReader subscriptionsReader;
    private SedpBuiltinSubscriptionsWriter subscriptionsWriter;
    private SedpBuiltinPublicationsReader publicationsReader;
    private SedpBuiltinPublicationsWriter publicationsWriter;
    private XLogger logger;

    public SedpBuiltinEndpointsConfigurator(
            TracingToken tracingToken,
            SedpBuiltinSubscriptionsReader subscriptionsReader,
            SedpBuiltinSubscriptionsWriter subscriptionsWriter,
            SedpBuiltinPublicationsReader publicationsReader,
            SedpBuiltinPublicationsWriter publicationsWriter) {
        this.subscriptionsReader = subscriptionsReader;
        this.subscriptionsWriter = subscriptionsWriter;
        this.publicationsReader = publicationsReader;
        this.publicationsWriter = publicationsWriter;
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    @Override
    public void onNext(RtpsTalkParameterListMessage participantDataMessage) {
        logger.entering("onNext");
        PARTICIPANTS_COUNT_METER.add(1);
        participantDataMessage
                .parameterList()
                .map(ParameterList::getProtocolParameters)
                .ifPresent(this::configureEndpoints);
        subscription.request(1);
        logger.exiting("onNext");
    }

    @Override
    public void onError(Throwable throwable) {
        logger.severe(throwable);
    }

    @Override
    public void onComplete() {}

    private void configureEndpoints(ProtocolParameterMap participantData) {
        var guid =
                participantData
                        .getFirstParameter(ParameterId.PID_PARTICIPANT_GUID, Guid.class)
                        .orElse(null);
        if (guid == null) {
            logger.warning("Received participant data without PID_PARTICIPANT_GUID");
            return;
        }
        logger.info("Configuring builtin endpoints for Participant {0}", guid);
        var availableEndpoints =
                participantData
                        .getFirstParameter(
                                ParameterId.PID_BUILTIN_ENDPOINT_SET, BuiltinEndpointSet.class)
                        .orElse(null);
        if (availableEndpoints == null) {
            logger.fine("Participant has no supported builtin endpoints, ignoring...");
            return;
        }
        var unicast =
                participantData.getParameters(
                        ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR, Locator.class);
        if (unicast.isEmpty()) {
            logger.fine("Participant has no locator defined, ignoring...");
        }
        configure(
                availableEndpoints,
                guid.guidPrefix,
                subscriptionsReader,
                null,
                Endpoint.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_ANNOUNCER,
                unicast);
        configure(
                availableEndpoints,
                guid.guidPrefix,
                null,
                subscriptionsWriter,
                Endpoint.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_DETECTOR,
                unicast);
        configure(
                availableEndpoints,
                guid.guidPrefix,
                publicationsReader,
                null,
                Endpoint.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_ANNOUNCER,
                unicast);
        configure(
                availableEndpoints,
                guid.guidPrefix,
                null,
                publicationsWriter,
                Endpoint.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_DETECTOR,
                unicast);
    }

    /** Configures all local builtin endpoints to work with new remote participant */
    @RtpsSpecReference(
            paragraph = "8.5.4.2",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "According to the DDS specification, the reliability QoS for these built-in"
                            + " Entities is set to reliable.")
    private void configure(
            BuiltinEndpointSet availableRemoteEndpoints,
            GuidPrefix guidPrefix,
            StatefullReliableRtpsReader<RtpsTalkParameterListMessage> reader,
            StatefullReliableRtpsWriter<RtpsTalkParameterListMessage> writer,
            Endpoint remoteEndpoint,
            List<Locator> unicast) {
        if (remoteEndpoint.getType() == EndpointType.READER) {
            Preconditions.notNull(writer, "Writer endpoint requires non null writer");
        }
        if (remoteEndpoint.getType() == EndpointType.WRITER) {
            Preconditions.notNull(reader, "Reader endpoint requires non null reader");
        }
        if (!availableRemoteEndpoints.hasEndpoint(remoteEndpoint)) {
            logger.fine("Participant does not support {0} endpoint, ignoring...", remoteEndpoint);
            return;
        }
        logger.fine("Configuring remote endpoint {0}...", remoteEndpoint);
        var remoteGuid = new Guid(guidPrefix, remoteEndpoint.getEntityId().getValue());
        switch (remoteEndpoint.getType()) {
            case WRITER:
                reader.matchedWriterAdd(remoteGuid, unicast);
                break;
            case READER:
                try {
                    // use RELIABLE as per @RtpsSpecReference above
                    writer.matchedReaderAdd(
                            remoteGuid,
                            unicast,
                            new ReaderQosPolicySet(
                                    ReliabilityQosPolicy.Kind.RELIABLE,
                                    DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS));
                } catch (IOException e) {
                    logger.severe("Remote endpoint " + remoteEndpoint + " configuration failed", e);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void close() {
        subscription.cancel();
        logger.fine("Closed");
    }
}
