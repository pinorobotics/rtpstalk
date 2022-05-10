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
package pinorobotics.rtpstalk.discovery.spdp;

import id.xfunction.Preconditions;
import id.xfunction.logging.XLogger;
import java.net.NetworkInterface;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.RtpsNetworkInterface;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiverFactory;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class SpdpService implements AutoCloseable {

    private RtpsTalkConfiguration config;
    private RtpsMessageReceiver receiver;
    private SpdpBuiltinParticipantReader reader;
    private SpdpBuiltinParticipantWriter writer;
    private boolean isStarted;
    private DataChannelFactory channelFactory;
    private SpdpDiscoveredParticipantDataFactory spdpDiscoveredDataFactory;
    private XLogger logger;
    private RtpsMessageReceiverFactory receiverFactory;

    public SpdpService(
            RtpsTalkConfiguration config,
            DataChannelFactory channelFactory,
            RtpsMessageReceiverFactory receiverFactory) {
        this(config, channelFactory, receiverFactory, new SpdpDiscoveredParticipantDataFactory());
    }

    public SpdpService(
            RtpsTalkConfiguration config,
            DataChannelFactory channelFactory,
            RtpsMessageReceiverFactory receiverFactory,
            SpdpDiscoveredParticipantDataFactory spdpDiscoveredDataFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
        this.receiverFactory = receiverFactory;
        this.spdpDiscoveredDataFactory = spdpDiscoveredDataFactory;
    }

    public void start(
            TracingToken tracingToken,
            RtpsNetworkInterface iface,
            NetworkInterface networkInterface,
            Subscriber<ParameterList> participantsSubscriber)
            throws Exception {
        Preconditions.isTrue(!isStarted, "Already started");
        tracingToken = new TracingToken(tracingToken, networkInterface.getName());
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
        logger.entering("start");
        receiver =
                receiverFactory.newRtpsMessageReceiver(
                        new TracingToken(tracingToken, getClass().getSimpleName()));
        logger.fine("Starting SPDP service on {0}", networkInterface.getName());
        reader =
                new SpdpBuiltinParticipantReader(
                        config, tracingToken, config.guidPrefix(), iface.getOperatingEntities());
        reader.subscribe(participantsSubscriber);
        Locator metatrafficMulticastLocator =
                Locator.createDefaultMulticastLocator(config.domainId());
        var dataChannel =
                channelFactory.bindMulticast(
                        tracingToken, networkInterface, metatrafficMulticastLocator);
        receiver.start(dataChannel);
        receiver.subscribe(reader);
        writer =
                new SpdpBuiltinParticipantWriter(
                        tracingToken, config, channelFactory, networkInterface);
        writer.readerLocatorAdd(metatrafficMulticastLocator);
        writer.setSpdpDiscoveredParticipantData(
                spdpDiscoveredDataFactory.createData(
                        config,
                        iface.getLocalMetatrafficUnicastLocator(),
                        iface.getLocalDefaultUnicastLocator()));
        writer.start();
        isStarted = true;
    }

    @Override
    public void close() {
        if (!isStarted) return;
        receiver.close();
        writer.close();
        reader.close();
        logger.fine("Closed");
    }
}
