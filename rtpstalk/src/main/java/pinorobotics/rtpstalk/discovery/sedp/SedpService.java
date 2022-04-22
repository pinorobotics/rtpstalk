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

import id.xfunction.Preconditions;
import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Flow.Publisher;
import pinorobotics.rtpstalk.RtpsNetworkInterface;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.liveliness.BuiltinParticipantMessageReader;
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.behavior.writer.StatefullRtpsWriter;
import pinorobotics.rtpstalk.messages.BuiltinEndpointQos.EndpointQos;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet.Endpoint;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet.EndpointType;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;

/**
 * Using the SPDPbuiltinParticipantReader, a local Participant local_participant discovers the
 * existence of another Participant described by the DiscoveredParticipantData participant_data. The
 * discovered Participant uses the SEDP (8.5.5.1 Discovery of a new remote Participant)
 */
public class SedpService extends SimpleSubscriber<ParameterList> implements AutoCloseable {

    private static final XLogger LOGGER = XLogger.getLogger(SedpService.class);
    private RtpsTalkConfiguration config;
    private SedpBuiltinSubscriptionsReader subscriptionsReader;
    private SedpBuiltinSubscriptionsWriter subscriptionsWriter;
    private SedpBuiltinPublicationsReader publicationsReader;
    private SedpBuiltinPublicationsWriter publicationsWriter;
    private RtpsMessageReceiver metatrafficReceiver;
    private boolean isStarted;
    private DataChannelFactory channelFactory;
    private RtpsNetworkInterface iface;

    public SedpService(RtpsTalkConfiguration config, DataChannelFactory channelFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
        metatrafficReceiver = new RtpsMessageReceiver("SedpServiceReceiver");
    }

    public void start(Publisher<ParameterList> participantsPublisher, RtpsNetworkInterface iface)
            throws IOException {
        LOGGER.entering("start");
        Preconditions.isTrue(!isStarted, "Already started");
        LOGGER.fine(
                "Starting SEDP service on {0} using following configuration: {1}",
                iface.getName(), config);

        subscriptionsWriter =
                new SedpBuiltinSubscriptionsWriter(
                        config, iface.getName(), channelFactory, iface.getOperatingEntities());
        publicationsWriter =
                new SedpBuiltinPublicationsWriter(
                        config, iface.getName(), channelFactory, iface.getOperatingEntities());
        subscriptionsReader =
                new SedpBuiltinSubscriptionsReader(
                        config, iface.getName(), iface.getOperatingEntities());
        metatrafficReceiver.subscribe(subscriptionsReader);
        publicationsReader =
                new SedpBuiltinPublicationsReader(
                        config, iface.getName(), iface.getOperatingEntities());
        metatrafficReceiver.subscribe(publicationsReader);
        if (config.builtinEndpointQos() == EndpointQos.NONE)
            metatrafficReceiver.subscribe(
                    new BuiltinParticipantMessageReader(
                            config, iface.getName(), iface.getOperatingEntities()));
        participantsPublisher.subscribe(this);
        metatrafficReceiver.start(channelFactory.bind(iface.getLocalMetatrafficUnicastLocator()));
        this.iface = iface;
        isStarted = true;
    }

    @Override
    public void onNext(ParameterList participantData) {
        LOGGER.entering("onNext");
        configureEndpoints(participantData);
        subscription.request(1);
        LOGGER.exiting("onNext");
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.severe(throwable);
    }

    @Override
    public void onComplete() {}

    public StatefullRtpsReader<ParameterList> getPublicationsReader() {
        return publicationsReader;
    }

    public StatefullRtpsReader<ParameterList> getSubscriptionsReader() {
        return subscriptionsReader;
    }

    private void configureEndpoints(ParameterList participantData) {
        var guid = (Guid) participantData.params.get(ParameterId.PID_PARTICIPANT_GUID);
        if (guid == null) {
            LOGGER.warning("Received participant data without PID_PARTICIPANT_GUID");
            return;
        }
        LOGGER.fine("Configuring builtin endpoints for Participant {0}", guid.guidPrefix);
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
                LOGGER.fine("Participant has no locator defined, ignoring...");
            }
        } else {
            LOGGER.fine("Participant has no supported builtin endpoints, ignoring...");
        }
    }

    /** Configures all local builtin endpoints to work with new remote participant */
    private void configure(
            BuiltinEndpointSet availableRemoteEndpoints,
            GuidPrefix guidPrefix,
            StatefullRtpsReader<ParameterList> reader,
            StatefullRtpsWriter<ParameterList> writer,
            Endpoint remoteEndpoint,
            List<Locator> unicast) {
        if (remoteEndpoint.getType() == EndpointType.READER) {
            Preconditions.notNull(writer, "Writer endpoint requires non null writer");
        }
        if (remoteEndpoint.getType() == EndpointType.WRITER) {
            Preconditions.notNull(reader, "Reader endpoint requires non null reader");
        }
        if (!availableRemoteEndpoints.hasEndpoint(remoteEndpoint)) {
            LOGGER.fine("Participant does not support {0} endpoint, ignoring...", remoteEndpoint);
            return;
        }
        LOGGER.fine("Configuring remote endpoint {0}...", remoteEndpoint);
        var remoteGuid = new Guid(guidPrefix, remoteEndpoint.getEntityId().getValue());
        switch (remoteEndpoint.getType()) {
            case WRITER:
                reader.matchedWriterAdd(remoteGuid, unicast);
                break;
            case READER:
                try {
                    writer.matchedReaderAdd(remoteGuid, unicast);
                } catch (IOException e) {
                    LOGGER.severe("Remote endpoint " + remoteEndpoint + " configuration failed", e);
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    public StatefullRtpsWriter<ParameterList> getSubscriptionsWriter() {
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
        subscriptionsReader.close();
        subscriptionsWriter.close();
        publicationsReader.close();
        publicationsWriter.close();
        metatrafficReceiver.close();
        LOGGER.fine("Closed");
    }
}
