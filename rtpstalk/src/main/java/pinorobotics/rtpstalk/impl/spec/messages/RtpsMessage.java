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
package pinorobotics.rtpstalk.impl.spec.messages;

import id.xfunction.XJsonStringBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Submessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsMessage implements HasStreamedFields {
    static final List<String> STREAMED_FIELDS = List.of("header", "submessages");
    public Header header;
    public Submessage[] submessages;

    public RtpsMessage() {}

    public RtpsMessage(Header header, Submessage... submessages) {
        this.header = header;
        this.submessages = submessages;
    }

    public RtpsMessage(Header header, List<Submessage> submessages) {
        this.header = header;
        this.submessages = submessages.toArray(new Submessage[0]);
    }

    public Submessage[] getSubmessages() {
        return submessages;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(submessages);
        result = prime * result + Objects.hash(header);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        RtpsMessage other = (RtpsMessage) obj;
        return Objects.equals(header, other.header)
                && Arrays.equals(submessages, other.submessages);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("header", header);
        builder.append("submessages", submessages);
        return builder.toString();
    }
}
