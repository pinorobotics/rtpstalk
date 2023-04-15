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
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.EndpointQos;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.behavior.liveliness.BuiltinParticipantMessageReader;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.discovery.spdp.SpdpBuiltinParticipantReader;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.impl.spec.transport.MetatrafficUnicastReceiver;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiverFactory;

/**
 * Manages metatraffic unicast locator.
 *
 * <p>Using the SPDPbuiltinParticipantReader, a local Participant local_participant discovers the
 * existence of another Participant described by the DiscoveredParticipantData participant_data. The
 * discovered Participant uses the SEDP
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
@RtpsSpecReference(
        paragraph = "8.5.5.1",
        protocolVersion = Predefined.Version_2_3,
        text = "Discovery of a new remote Participant")
public class MetatrafficUnicastService implements AutoCloseable {
    private TracingToken tracingToken;
    private RtpsTalkConfigurationInternal config;
    private SedpBuiltinSubscriptionsReader subscriptionsReader;
    private SedpBuiltinSubscriptionsWriter subscriptionsWriter;
    private SedpBuiltinPublicationsReader publicationsReader;
    private SedpBuiltinPublicationsWriter publicationsWriter;
    private List<SedpBuiltinEndpointsConfigurator> configurators = new ArrayList<>();
    private SpdpBuiltinParticipantReader spdpReader;
    private MetatrafficUnicastReceiver metatrafficUnicastReceiver;
    private boolean isStarted;
    private DataChannelFactory channelFactory;
    private RtpsNetworkInterface iface;
    private XLogger logger;
    private RtpsMessageReceiverFactory receiverFactory;
    private Executor publisherExecutor;

    public MetatrafficUnicastService(
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
        this.tracingToken = tracingToken;
        this.iface = iface;
        logger = XLogger.getLogger(getClass(), tracingToken);
        logger.entering("start");
        logger.fine(
                "Starting metatraffic unicast service on {0}",
                iface.getLocalMetatrafficUnicastLocator());
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
        registerSpdpReader();
        if (config.publicConfig().builtinEndpointQos() == EndpointQos.NONE)
            metatrafficUnicastReceiver.subscribe(
                    new BuiltinParticipantMessageReader(
                            config.publicConfig(),
                            tracingToken,
                            publisherExecutor,
                            iface.getOperatingEntities()));
        metatrafficUnicastReceiver.start(iface.getMetatrafficUnicastChannel());
        isStarted = true;
        spdpReader.subscribe(newSedpConfigurator());
    }

    @RtpsSpecReference(
            paragraph = "8.5.3.1",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "The pre-configured list of locators may include both unicast and multicast"
                            + " locators.")
    private void registerSpdpReader() {
        spdpReader =
                new SpdpBuiltinParticipantReader(
                        config.publicConfig(),
                        new TracingToken(tracingToken, "sedp"),
                        publisherExecutor,
                        config.publicConfig().guidPrefix(),
                        iface.getOperatingEntities(),
                        iface.getParticipantsRegistry());
        metatrafficUnicastReceiver.subscribe(spdpReader);
    }

    public StatefullReliableRtpsReader<RtpsTalkParameterListMessage> getPublicationsReader() {
        return publicationsReader;
    }

    public StatefullReliableRtpsReader<RtpsTalkParameterListMessage> getSubscriptionsReader() {
        return subscriptionsReader;
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
        configurators.forEach(SedpBuiltinEndpointsConfigurator::close);
        subscriptionsWriter.close();
        // close Reader only after the Writer so that Writer could properly ack any
        // data which is still present in its cache
        subscriptionsReader.close();
        publicationsReader.close();
        publicationsWriter.close();
        spdpReader.close();
        metatrafficUnicastReceiver.close();
        logger.fine("Closed");
    }

    public Subscriber<RtpsTalkParameterListMessage> newSedpConfigurator() {
        Preconditions.isTrue(isStarted, "Metatraffic unicast service is not started");
        logger.fine("Creating new SEDP configurator");
        var configurator =
                new SedpBuiltinEndpointsConfigurator(
                        tracingToken,
                        subscriptionsReader,
                        subscriptionsWriter,
                        publicationsReader,
                        publicationsWriter);
        configurators.add(configurator);
        return configurator;
    }
}
