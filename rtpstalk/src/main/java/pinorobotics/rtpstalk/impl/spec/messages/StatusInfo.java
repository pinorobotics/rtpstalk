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
package pinorobotics.rtpstalk.impl.spec.messages;

import id.xfunction.XJsonStringBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class StatusInfo {

    public int value;

    public StatusInfo() {}

    public StatusInfo(int value) {
        this.value = value;
    }

    public List<String> getFlags() {
        var flags = new ArrayList<String>();
        if (isDisposed()) flags.add("isDisposed");
        if (isUnregistered()) flags.add("isUnregistered");
        return flags;
    }

    /**
     * Indicates that the DDS DataWriter has disposed the instance of the data-object whose Key
     * appears in the submessage.
     */
    public boolean isDisposed() {
        return (value & 1) != 0;
    }

    /**
     * Indicates that the DDS DataWriter has unregistered the instance of the data-object whose Key
     * appears in the submessage.
     */
    public boolean isUnregistered() {
        return (value & 2) != 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        StatusInfo other = (StatusInfo) obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", getFlags());
        return builder.toString();
    }
}
