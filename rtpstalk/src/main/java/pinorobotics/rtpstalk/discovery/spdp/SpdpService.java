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
import java.util.concurrent.Flow.Publisher;
import pinorobotics.rtpstalk.RtpsNetworkInterface;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;

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

    public SpdpService(RtpsTalkConfiguration config, DataChannelFactory channelFactory) {
        this(config, channelFactory, new SpdpDiscoveredParticipantDataFactory());
    }

    public SpdpService(
            RtpsTalkConfiguration config,
            DataChannelFactory channelFactory,
            SpdpDiscoveredParticipantDataFactory spdpDiscoveredDataFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
        this.spdpDiscoveredDataFactory = spdpDiscoveredDataFactory;
    }

    public void start(TracingToken tracingToken, RtpsNetworkInterface iface) throws Exception {
        Preconditions.isTrue(!isStarted, "Already started");
        tracingToken = new TracingToken(tracingToken, iface.getName());
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
        logger.entering("start");
        receiver =
                new RtpsMessageReceiver(new TracingToken(tracingToken, getClass().getSimpleName()));
        logger.fine(
                "Starting SPDP service on {0} using following configuration: {1}",
                iface.getName(), config);
        reader =
                new SpdpBuiltinParticipantReader(
                        tracingToken, config.guidPrefix(), iface.getOperatingEntities());
        var dataChannel = channelFactory.bind(iface.getLocalMetatrafficMulticastLocator());
        receiver.start(dataChannel);
        receiver.subscribe(reader);
        writer = new SpdpBuiltinParticipantWriter(config, channelFactory, tracingToken);
        writer.readerLocatorAdd(iface.getLocalMetatrafficMulticastLocator());
        writer.setSpdpDiscoveredParticipantData(
                spdpDiscoveredDataFactory.createData(
                        config,
                        iface.getLocalMetatrafficUnicastLocator(),
                        iface.getLocalDefaultUnicastLocator()));
        writer.start();
        isStarted = true;
    }

    public Publisher<ParameterList> getParticipantsPublisher() {
        Preconditions.isTrue(isStarted, "Service not yet started");
        return reader;
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
