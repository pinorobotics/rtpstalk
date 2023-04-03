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

import id.xfunction.Preconditions;
import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import pinorobotics.rtpstalk.EndpointQos;
import pinorobotics.rtpstalk.RtpsTalkMetrics;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.behavior.liveliness.BuiltinParticipantMessageReader;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet.Endpoint;
import pinorobotics.rtpstalk.impl.spec.messages.BuiltinEndpointSet.EndpointType;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy.Kind;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.impl.spec.transport.MetatrafficUnicastReceiver;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiverFactory;

/**
 * Using the SPDPbuiltinParticipantReader, a local Participant local_participant discovers the
 * existence of another Participant described by the DiscoveredParticipantData participant_data. The
 * discovered Participant uses the SEDP
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
@RtpsSpecReference(
        paragraph = "8.5.5.1",
        protocolVersion = Predefined.Version_2_3,
        text = "Discovery of a new remote Participant")
public class SedpService extends SimpleSubscriber<RtpsTalkParameterListMessage>
        implements AutoCloseable {
    private final Meter METER = GlobalOpenTelemetry.getMeter(SedpService.class.getSimpleName());
    private final LongCounter PARTICIPANTS_COUNT_METER =
            METER.counterBuilder(RtpsTalkMetrics.PARTICIPANTS_COUNT_METRIC)
                    .setDescription(RtpsTalkMetrics.PARTICIPANTS_COUNT_METRIC_DESCRIPTION)
                    .build();
    private RtpsTalkConfigurationInternal config;
    private SedpBuiltinSubscriptionsReader subscriptionsReader;
    private SedpBuiltinSubscriptionsWriter subscriptionsWriter;
    private SedpBuiltinPublicationsReader publicationsReader;
    private SedpBuiltinPublicationsWriter publicationsWriter;
    private MetatrafficUnicastReceiver metatrafficUnicastReceiver;
    private boolean isStarted;
    private DataChannelFactory channelFactory;
    private RtpsNetworkInterface iface;
    private XLogger logger;
    private RtpsMessageReceiverFactory receiverFactory;
    private Executor publisherExecutor;

    public SedpService(
            RtpsTalkConfigurationInternal config,
            Executor publisherExecutor,
            DataChannelFactory channelFactory,
            RtpsMessageReceiverFactory receiverFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
        this.publisherExecutor = publisherExecutor;
        this.receiverFactory = receiverFactory;
    }

    public void start(TracingToken tracingToken, RtpsNetworkInterface iface) throws IOException {
        Preconditions.isTrue(!isStarted, "Already started");
        logger = XLogger.getLogger(getClass(), tracingToken);
        logger.entering("start");
        logger.fine("Starting SEDP service on {0}", iface.getLocalMetatrafficUnicastLocator());
        metatrafficUnicastReceiver =
                receiverFactory.newMetatrafficUnicastReceiver(
                        config.publicConfig(), tracingToken, publisherExecutor);
        subscriptionsWriter =
                new SedpBuiltinSubscriptionsWriter(
                        config,
                        tracingToken,
                        publisherExecutor,
                        channelFactory,
                        iface.getOperatingEntities());
        metatrafficUnicastReceiver.subscribe(subscriptionsWriter.getWriterReader());
        publicationsWriter =
                new SedpBuiltinPublicationsWriter(
                        config,
                        tracingToken,
                        publisherExecutor,
                        channelFactory,
                        iface.getOperatingEntities());
        metatrafficUnicastReceiver.subscribe(publicationsWriter.getWriterReader());
        subscriptionsReader =
                new SedpBuiltinSubscriptionsReader(
                        config.publicConfig(),
                        tracingToken,
                        publisherExecutor,
                        iface.getOperatingEntities());
        metatrafficUnicastReceiver.subscribe(subscriptionsReader);
        publicationsReader =
                new SedpBuiltinPublicationsReader(
                        config.publicConfig(),
                        tracingToken,
                        publisherExecutor,
                        iface.getOperatingEntities());
        metatrafficUnicastReceiver.subscribe(publicationsReader);
        if (config.publicConfig().builtinEndpointQos() == EndpointQos.NONE)
            metatrafficUnicastReceiver.subscribe(
                    new BuiltinParticipantMessageReader(
                            config.publicConfig(),
                            tracingToken,
                            publisherExecutor,
                            iface.getOperatingEntities()));
        metatrafficUnicastReceiver.start(iface.getMetatrafficUnicastChannel());
        this.iface = iface;
        isStarted = true;
    }

    @Override
    public void onNext(RtpsTalkParameterListMessage participantDataMessage) {
        logger.entering("onNext");
        PARTICIPANTS_COUNT_METER.add(1);
        participantDataMessage.parameterList().ifPresent(this::configureEndpoints);
        subscription.request(1);
        logger.exiting("onNext");
    }

    @Override
    public void onError(Throwable throwable) {
        logger.severe(throwable);
    }

    @Override
    public void onComplete() {}

    public StatefullReliableRtpsReader<RtpsTalkParameterListMessage> getPublicationsReader() {
        return publicationsReader;
    }

    public StatefullReliableRtpsReader<RtpsTalkParameterListMessage> getSubscriptionsReader() {
        return subscriptionsReader;
    }

    private void configureEndpoints(ParameterList participantData) {
        var guid = (Guid) participantData.getParameters().get(ParameterId.PID_PARTICIPANT_GUID);
        if (guid == null) {
            logger.warning("Received participant data without PID_PARTICIPANT_GUID");
            return;
        }
        logger.fine("Configuring builtin endpoints for Participant {0}", guid.guidPrefix);
        var params = participantData.getParameters();
        var value = params.get(ParameterId.PID_BUILTIN_ENDPOINT_SET);
        if (value instanceof BuiltinEndpointSet availableEndpoints) {
            if (params.get(ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR)
                    instanceof Locator locator) {
                var unicast = List.of(locator);
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
            } else {
                logger.fine("Participant has no locator defined, ignoring...");
            }
        } else {
            logger.fine("Participant has no supported builtin endpoints, ignoring...");
        }
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
                    writer.matchedReaderAdd(remoteGuid, unicast, Kind.RELIABLE);
                } catch (IOException e) {
                    logger.severe("Remote endpoint " + remoteEndpoint + " configuration failed", e);
                }
                break;
            default:
                break;
        }
    }

    public StatefullReliableRtpsWriter<RtpsTalkParameterListMessage> getSubscriptionsWriter() {
        return subscriptionsWriter;
    }

    public SedpBuiltinPublicationsWriter getPublicationsWriter() {
        return publicationsWriter;
    }

    public RtpsNetworkInterface getNetworkInterface() {
        return iface;
    }

    @Override
    public void close() {
        if (!isStarted) return;
        subscription.cancel();
        subscriptionsWriter.close();
        // close Reader only after the Writer so that Writer could properly ack any
        // data which is still present in its cache
        subscriptionsReader.close();
        publicationsReader.close();
        publicationsWriter.close();
        metatrafficUnicastReceiver.close();
        logger.fine("Closed");
    }
}
