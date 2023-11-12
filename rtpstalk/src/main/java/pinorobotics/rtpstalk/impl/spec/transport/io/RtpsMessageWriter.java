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
package pinorobotics.rtpstalk.impl.spec.transport.io;

import id.kineticstreamer.KineticStreamWriter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.RtpsTalkMetrics;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsMessageWriter {
    private final Meter METER =
            GlobalOpenTelemetry.getMeter(RtpsMessageWriter.class.getSimpleName());
    private final LongHistogram WRITE_TIME_METER =
            METER.histogramBuilder(RtpsTalkMetrics.WRITE_TIME_METRIC)
                    .setDescription(RtpsTalkMetrics.WRITE_TIME_METRIC_DESCRIPTION)
                    .ofLongs()
                    .build();

    public void writeRtpsMessage(RtpsMessage data, ByteBuffer buf) throws Exception {
        var out = new RtpsOutputKineticStream(buf.order(RtpsTalkConfiguration.getByteOrder()));
        var ksw =
                new KineticStreamWriter(out)
                        .withController(new RtpsKineticStreamWriterController());
        out.setWriter(ksw);
        var startAt = Instant.now();
        try {
            ksw.write(data);
        } finally {
            WRITE_TIME_METER.record(Duration.between(startAt, Instant.now()).toMillis());
        }
    }
}
