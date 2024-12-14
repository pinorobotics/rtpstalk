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
package pinorobotics.rtpstalk.impl.spec.transport.io;

import id.kineticstreamer.KineticStreamController;
import id.kineticstreamer.KineticStreamWriter;
import id.kineticstreamer.PublicStreamedFieldsProvider;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.metrics.RtpsTalkMetrics;

/**
 * Thread-safe
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsMessageWriter {
    private final Meter METER =
            GlobalOpenTelemetry.getMeter(RtpsMessageWriter.class.getSimpleName());
    private final LongHistogram WRITE_TIME_METER =
            METER.histogramBuilder(RtpsTalkMetrics.DESERIALIZATION_TIME_METRIC)
                    .setDescription(RtpsTalkMetrics.WRITE_TIME_METRIC_DESCRIPTION)
                    .ofLongs()
                    .build();
    private KineticStreamController controller =
            new RtpsKineticStreamController()
                    .withFieldsProvider(
                            new PublicStreamedFieldsProvider(
                                    FieldsOrderedByNameProvider::readOrderedFieldNames));

    /** Write RTPS message to stream of bytes to be sent over the network. */
    public void writeRtpsMessage(RtpsMessage message, ByteBuffer buf) throws Exception {
        var startAt = Instant.now();
        try {
            write(message, buf.order(RtpsTalkConfiguration.getByteOrder()));
        } finally {
            WRITE_TIME_METER.record(Duration.between(startAt, Instant.now()).toMillis());
        }
    }

    /**
     * Write RTPS message to stream of bytes.
     *
     * @param message can be full RTPS message or particular part of it
     */
    public <T> void write(T message, ByteBuffer buf) throws Exception {
        var out = new RtpsOutputKineticStream(buf);
        var ksw = new KineticStreamWriter(out).withController(controller);
        out.setWriter(ksw);
        ksw.write(message);
    }
}
