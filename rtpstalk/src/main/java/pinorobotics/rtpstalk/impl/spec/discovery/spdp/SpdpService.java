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
package pinorobotics.rtpstalk.impl.spec.discovery.spdp;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.net.NetworkInterface;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.spec.DataFactory;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiver;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiverFactory;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SpdpService implements AutoCloseable {

    private RtpsTalkConfigurationInternal config;
    private RtpsMessageReceiver receiver;
    private SpdpBuiltinParticipantReader reader;
    private SpdpBuiltinParticipantWriter writer;
    private boolean isStarted;
    private DataChannelFactory channelFactory;
    private SpdpDiscoveredParticipantDataFactory spdpDiscoveredDataFactory;
    private XLogger logger;
    private RtpsMessageReceiverFactory receiverFactory;
    private Executor publisherExecutor;
    private DataFactory dataFactory;

    public SpdpService(
            RtpsTalkConfigurationInternal config,
            Executor publisherExecutor,
            DataChannelFactory channelFactory,
            RtpsMessageReceiverFactory receiverFactory) {
        this(
                config,
                publisherExecutor,
                channelFactory,
                receiverFactory,
                new SpdpDiscoveredParticipantDataFactory());
    }

    public SpdpService(
            RtpsTalkConfigurationInternal config,
            Executor publisherExecutor,
            DataChannelFactory channelFactory,
            RtpsMessageReceiverFactory receiverFactory,
            SpdpDiscoveredParticipantDataFactory spdpDiscoveredDataFactory) {
        this.config = config;
        this.publisherExecutor = publisherExecutor;
        this.channelFactory = channelFactory;
        this.receiverFactory = receiverFactory;
        this.spdpDiscoveredDataFactory = spdpDiscoveredDataFactory;
        dataFactory = new DataFactory();
    }

    public void start(
            TracingToken tracingToken,
            RtpsNetworkInterface iface,
            NetworkInterface networkInterface,
            Subscriber<RtpsTalkParameterListMessage> participantsSubscriber)
            throws Exception {
        Preconditions.isTrue(!isStarted, "Already started");
        tracingToken = new TracingToken(tracingToken, networkInterface.getName());
        logger = XLogger.getLogger(getClass(), tracingToken);
        logger.entering("start");
        receiver =
                receiverFactory.newRtpsMessageReceiver(
                        config.publicConfig(),
                        new TracingToken(tracingToken, "SpdpReceiver"),
                        publisherExecutor);
        logger.fine("Starting SPDP service on {0}", networkInterface.getName());
        reader =
                new SpdpBuiltinParticipantReader(
                        config.publicConfig(),
                        tracingToken,
                        publisherExecutor,
                        config.publicConfig().guidPrefix(),
                        iface.getOperatingEntities());
        reader.subscribe(participantsSubscriber);
        Locator metatrafficMulticastLocator =
                Locator.createDefaultMulticastLocator(config.publicConfig().domainId());
        var dataChannel =
                channelFactory.bindMulticast(
                        tracingToken, networkInterface, metatrafficMulticastLocator);
        receiver.start(dataChannel);
        receiver.subscribe(reader);
        writer =
                new SpdpBuiltinParticipantWriter(
                        config.publicConfig(),
                        tracingToken,
                        publisherExecutor,
                        channelFactory,
                        networkInterface);
        writer.readerLocatorAdd(metatrafficMulticastLocator);
        writer.setSpdpDiscoveredParticipantDataMessage(
                new RtpsTalkParameterListMessage(
                        spdpDiscoveredDataFactory.createData(
                                config,
                                iface.getLocalMetatrafficUnicastLocator(),
                                iface.getLocalDefaultUnicastLocator())));
        writer.start();
        isStarted = true;
    }

    @Override
    public void close() {
        if (!isStarted) return;
        if (!writer.isClosed()) {
            writer.newChange(
                    RtpsTalkParameterListMessage.withInlineQosOnly(
                            dataFactory.createReaderDisposedSubscriptionData(
                                    config.localParticipantGuid())));
        }
        receiver.close();
        writer.close();
        reader.close();
        logger.fine("Closed");
    }
}
