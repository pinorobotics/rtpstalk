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
package pinorobotics.rtpstalk.impl.spec.discovery.spdp;

import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import java.io.IOException;
import java.net.NetworkInterface;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.RtpsTalkParameterListMessage;
import pinorobotics.rtpstalk.impl.spec.behavior.ParticipantsRegistry;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatelessRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;
import pinorobotics.rtpstalk.metrics.RtpsTalkMetrics;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SpdpBuiltinParticipantWriter extends StatelessRtpsWriter<RtpsTalkParameterListMessage>
        implements Runnable, AutoCloseable {
    private final Meter METER =
            GlobalOpenTelemetry.getMeter(SpdpBuiltinParticipantWriter.class.getSimpleName());
    private final LongCounter ANNOUNCEMENTS_METER =
            METER.counterBuilder(RtpsTalkMetrics.ANNOUNCEMENTS_COUNT_METRIC)
                    .setDescription(RtpsTalkMetrics.ANNOUNCEMENTS_COUNT_METRIC_DESCRIPTION)
                    .build();
    private static final XLogger LOGGER = XLogger.getLogger(SpdpBuiltinParticipantWriter.class);
    private ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(
                    new NamedThreadFactory("SpdpBuiltinParticipantWriter"));
    private RtpsTalkParameterListMessage message;
    private Duration rate;
    private NetworkInterface networkInterface;
    private ParticipantsRegistry participantsRegistry;

    public SpdpBuiltinParticipantWriter(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            DataChannelFactory channelFactory,
            NetworkInterface networkInterface,
            ParticipantsRegistry participantsRegistry) {
        super(
                config,
                tracingToken,
                publisherExecutor,
                channelFactory,
                EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_ANNOUNCER.getValue(),
                EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue());
        this.networkInterface = networkInterface;
        this.participantsRegistry = participantsRegistry;
        this.rate = config.publicConfig().spdpDiscoveredParticipantDataPublishPeriod();
    }

    public void start() {
        executor.scheduleWithFixedDelay(this, 0, rate.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void readerLocatorAdd(Locator locator) throws IOException {
        var sender =
                new RtpsMessageSender(
                        getTracingToken(),
                        getChannelFactory()
                                .bindMulticast(getTracingToken(), networkInterface, locator),
                        new Guid(
                                GuidPrefix.Predefined.GUIDPREFIX_UNKNOWN.getValue(),
                                getReaderEntiyId()),
                        getGuid().entityId);
        subscribe(sender);
    }

    public void setSpdpDiscoveredParticipantDataMessage(RtpsTalkParameterListMessage message) {
        LOGGER.fine("Setting SpdpDiscoveredParticipantData {0}", message.parameterList());
        this.message = message;
    }

    @Override
    public void run() {
        if (executor.isShutdown()) return;
        participantsRegistry.removeParticipantsWithExpiredLease();
        if (message == null) {
            LOGGER.fine("No SpdpDiscoveredParticipantData to send, skipping");
            return;
        }
        switch ((int) getLastChangeNumber()) {
            case 0:
                newChange(message);
                break;
            case 1:
                repeatLastChange();
                break;
            default:
                throw new RuntimeException("Unexpected last change value " + getLastChangeNumber());
        }
        ANNOUNCEMENTS_METER.add(1);
        LOGGER.fine("Sent SpdpDiscoveredParticipantData");
    }

    @Override
    public void close() {
        super.close();
        executor.shutdown();
        LOGGER.fine("Closed");
    }
}
