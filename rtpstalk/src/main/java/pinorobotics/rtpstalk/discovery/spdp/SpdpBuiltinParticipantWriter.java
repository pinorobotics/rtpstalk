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

import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.XLogger;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.writer.StatelessRtpsWriter;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.transport.DataChannelFactory;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class SpdpBuiltinParticipantWriter extends StatelessRtpsWriter<ParameterList>
        implements Runnable, AutoCloseable {

    private static final XLogger LOGGER = XLogger.getLogger(SpdpBuiltinParticipantWriter.class);
    private ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(
                    new NamedThreadFactory("SpdpBuiltinParticipantWriter"));
    private ParameterList data;
    private Duration rate;

    public SpdpBuiltinParticipantWriter(
            DataChannelFactory channelFactory, RtpsTalkConfiguration config) {
        super(
                config,
                channelFactory,
                EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_ANNOUNCER.getValue(),
                EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue());
        this.rate = config.spdpDiscoveredParticipantDataPublishPeriod();
    }

    public void start() {
        executor.scheduleWithFixedDelay(this, 0, rate.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void setSpdpDiscoveredParticipantData(ParameterList data) {
        LOGGER.fine("Setting SpdpDiscoveredParticipantData {0}", data);
        this.data = data;
    }

    @Override
    public void run() {
        if (executor.isShutdown()) return;
        if (data == null) {
            LOGGER.fine("No SpdpDiscoveredParticipantData to send, skipping");
            return;
        }
        switch ((int) getLastChangeNumber()) {
            case 0:
                newChange(data);
                break;
            case 1:
                repeatLastChange();
                break;
            default:
                throw new RuntimeException("Unexpected last change value " + getLastChangeNumber());
        }
        LOGGER.fine("Sent SpdpDiscoveredParticipantData");
    }

    @Override
    public void close() {
        super.close();
        executor.shutdown();
    }
}
