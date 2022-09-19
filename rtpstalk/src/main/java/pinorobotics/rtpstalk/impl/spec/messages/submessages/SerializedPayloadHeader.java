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

import id.xfunction.XJsonStringBuilder;
import java.util.Arrays;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class SerializedPayloadHeader {

    private static final int REPRESENTATION_OPTIONS_SIZE = 2;
    public static final int SIZE = REPRESENTATION_OPTIONS_SIZE + RepresentationIdentifier.SIZE;

    public static final SerializedPayloadHeader DEFAULT_PARAMETER_LIST_HEADER =
            new SerializedPayloadHeader(RepresentationIdentifier.Predefined.PL_CDR_LE.getValue());
    public static final SerializedPayloadHeader DEFAULT_DATA_HEADER =
            new SerializedPayloadHeader(RepresentationIdentifier.Predefined.CDR_LE.getValue());

    public RepresentationIdentifier representation_identifier;

    public byte[] representation_options = new byte[REPRESENTATION_OPTIONS_SIZE];

    public SerializedPayloadHeader() {}

    public SerializedPayloadHeader(RepresentationIdentifier representation_identifier) {
        this.representation_identifier = representation_identifier;
    }

    public SerializedPayloadHeader(
            RepresentationIdentifier representation_identifier, byte[] representation_options) {
        this.representation_identifier = representation_identifier;
        this.representation_options = representation_options;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("representation_identifier", representation_identifier);
        builder.append("representation_options", Arrays.toString(representation_options));
        return builder.toString();
    }
}
