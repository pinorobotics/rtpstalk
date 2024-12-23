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
package pinorobotics.rtpstalk.impl.spec.messages.submessages;

import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.transport.io.LengthCalculator;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class InfoDestination extends Submessage {

    public static final int SIZE = GuidPrefix.SIZE;

    /**
     * Provides the GuidPrefix that should be used to reconstruct the GUIDs of all the RTPS Reader
     * entities whose EntityIds appears in the Submessages that follow
     */
    public GuidPrefix guidPrefix;

    public InfoDestination() {}

    public InfoDestination(GuidPrefix guidPrefix) {
        this.guidPrefix = guidPrefix;
        submessageHeader =
                new SubmessageHeader(
                        SubmessageKind.Predefined.INFO_DST.getValue(),
                        LengthCalculator.getInstance().getFixedLength(InfoDestination.class));
    }

    @Override
    protected Object[] getAdditionalFields() {
        return new Object[] {"guidPrefix", guidPrefix};
    }
}
