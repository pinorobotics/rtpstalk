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
package pinorobotics.rtpstalk.messages;

import java.util.Objects;

/** @author lambdaprime intid@protonmail.com */
public class DestinationOrderQosPolicy {

    public static enum Kind {
        BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS,
        BY_SOURCE_TIMESTAMP_DESTINATIONORDER_QOS;
    }

    public int kind;

    public DestinationOrderQosPolicy() {
        this(Kind.BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS);
    }

    public DestinationOrderQosPolicy(Kind kind) {
        this.kind = kind.ordinal();
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DestinationOrderQosPolicy other = (DestinationOrderQosPolicy) obj;
        return kind == other.kind;
    }

    @Override
    public String toString() {
        return Kind.values()[kind].toString();
    }
}
