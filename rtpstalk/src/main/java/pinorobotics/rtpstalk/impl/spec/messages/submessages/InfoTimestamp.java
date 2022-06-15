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
package pinorobotics.rtpstalk.impl.spec.messages.submessages;

import java.util.List;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.Timestamp;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class InfoTimestamp extends Submessage {

    public Timestamp timestamp;

    public InfoTimestamp() {}

    public InfoTimestamp(Timestamp timestamp) {
        submessageHeader =
                new SubmessageHeader(
                        SubmessageKind.Predefined.INFO_TS.getValue(),
                        LengthCalculator.getInstance().getFixedLength(InfoTimestamp.class));
        this.timestamp = timestamp;
    }

    @Override
    public List<String> getFlags() {
        var flags = super.getFlags();
        if (isInvalidate()) flags.add("InvalidateFlag");
        return flags;
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] {"timestamp", timestamp};
    }

    /** Subsequent Submessages should not be considered to have a valid timestamp. */
    private boolean isInvalidate() {
        return (getFlagsInternal() & 2) != 0;
    }

    public static InfoTimestamp now() {
        return new InfoTimestamp(Timestamp.now());
    }
}
