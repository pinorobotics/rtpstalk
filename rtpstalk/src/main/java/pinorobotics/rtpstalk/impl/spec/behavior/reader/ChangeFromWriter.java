/*
 * Copyright 2024 rtpstalk project
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
package pinorobotics.rtpstalk.impl.spec.behavior.reader;

import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
@RtpsSpecReference(
        protocolVersion = Predefined.Version_2_3,
        paragraph = "8.4.10.5",
        text = "RTPS ChangeFromWriter")
public record ChangeFromWriter(ChangeFromWriterStatusKind status, boolean isRelevant) {
    public static final ChangeFromWriter IRRELEVANT =
            new ChangeFromWriter(ChangeFromWriterStatusKind.RECEIVED, false);
    public static final ChangeFromWriter MISSING =
            new ChangeFromWriter(ChangeFromWriterStatusKind.MISSING, true);
    public static final ChangeFromWriter RECEIVED =
            new ChangeFromWriter(ChangeFromWriterStatusKind.RECEIVED, true);

    public boolean isReceived() {
        return status == ChangeFromWriterStatusKind.RECEIVED;
    }

    public boolean isMissing() {
        return status == ChangeFromWriterStatusKind.MISSING;
    }
}
