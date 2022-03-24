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

import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class SpdpService implements AutoCloseable {

    private static final XLogger LOGGER = XLogger.getLogger(SpdpService.class);
    private RtpsTalkConfiguration config;
    private RtpsMessageReceiver receiver;
    private SpdpBuiltinParticipantReader reader;
    private SpdpBuiltinParticipantWriter writer;
    private boolean isStarted;
    private DataChannelFactory channelFactory;
    private SpdpDiscoveredParticipantDataFactory spdpDiscoveredDataFactory;

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
        receiver = new RtpsMessageReceiver(getClass().getSimpleName());
    }

    public void start() throws Exception {
        LOGGER.entering("start");
        XAsserts.assertTrue(!isStarted, "Already started");
        LOGGER.fine("Using following configuration: {0}", config);
        var iface = config.networkInterfaces().get(0);
        reader =
                new SpdpBuiltinParticipantReader(config.guidPrefix(), iface.getOperatingEntities());
        var localMetatrafficMulticastLocator = iface.getLocalMetatrafficMulticastLocator();
        var dataChannel = channelFactory.bind(localMetatrafficMulticastLocator);
        receiver.start(dataChannel);
        receiver.subscribe(reader);
        writer = new SpdpBuiltinParticipantWriter(channelFactory, config);
        writer.readerLocatorAdd(localMetatrafficMulticastLocator);
        writer.setSpdpDiscoveredParticipantData(
                spdpDiscoveredDataFactory.createData(
                        config,
                        iface.getLocalMetatrafficUnicastLocator(),
                        iface.getLocalDefaultUnicastLocator()));
        writer.start();
        isStarted = true;
    }

    public SpdpBuiltinParticipantReader getReader() {
        XAsserts.assertTrue(isStarted, "Service not yet started");
        return reader;
    }

    @Override
    public void close() throws Exception {
        if (!isStarted) return;
        receiver.close();
        writer.close();
    }
}
