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
package pinorobotics.rtpstalk.messages.submessages;

import id.xfunction.XJsonStringBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Each RTPS Message consists of a variable number of RTPS Submessage parts.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public abstract class Submessage {

    /**
     * The SubmessageHeader identifies the kind of Submessage and the optional elements within that
     * Submessage.
     */
    public SubmessageHeader submessageHeader;

    public boolean isLittleEndian() {
        return (getFlagsInternal() & 1) == 1;
    }

    protected byte getFlagsInternal() {
        if (submessageHeader == null) return 0;
        return submessageHeader.submessageFlag;
    }

    public List<String> getFlags() {
        var flags = new ArrayList<String>();
        if (isLittleEndian()) flags.add("LittleEndian");
        else flags.add("BigEndian");
        return flags;
    }

    protected Object[] getAdditionalFields() {
        return new Object[0];
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("submessageHeader", submessageHeader);
        builder.append("flags", getFlags());
        builder.append(getAdditionalFields());
        return builder.toString();
    }
}
