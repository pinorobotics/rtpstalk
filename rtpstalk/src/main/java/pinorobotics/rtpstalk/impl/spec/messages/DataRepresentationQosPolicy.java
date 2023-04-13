/*
 * Copyright 2023 rtpstalk project
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
package pinorobotics.rtpstalk.impl.spec.messages;

import id.xfunction.XJsonStringBuilder;
import java.util.EnumSet;

/**
 * Currently not being used by rtpstalk.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataRepresentationQosPolicy {

    public static enum DataRepresentation {
        XCDR_DATA_REPRESENTATION(0),
        XML_DATA_REPRESENTATION(1),
        XCDR2_DATA_REPRESENTATION(2);

        private short value;

        private DataRepresentation(int value) {
            this.value = (short) value;
        }

        public short getValue() {
            return value;
        }
    };

    public ShortSequence value;

    public DataRepresentationQosPolicy() {}

    public DataRepresentationQosPolicy(ShortSequence value) {
        this.value = value;
    }

    public DataRepresentationQosPolicy(EnumSet<DataRepresentation> set) {
        var ar = new short[set.size()];
        int i = 0;
        for (var item : set) ar[i++] = item.value;
        this.value = new ShortSequence(ar);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", value);
        return builder.toString();
    }
}
