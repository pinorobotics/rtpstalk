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

import java.util.Objects;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class DurabilityQosPolicy {

    /**
     * @see <a
     *     href="https://fast-dds.docs.eprosima.com/en/latest/fastdds/api_reference/dds_pim/core/policy/durabilityqospolicykind.html">Fast-DDS
     *     explanation</a>
     */
    public static enum Kind {

        /**
         * The Service does not need to keep any samples of data-instances on behalf of any
         * DataReader that is not known by the DataWriter at the time the instance is written. In
         * other words the Service will only attempt to provide the data to existing subscribers
         */
        VOLATILE_DURABILITY_QOS,

        /**
         * For TRANSIENT_LOCAL, the service is only required to keep the data in the memory of the
         * DataWriter that wrote the data and the data is not required to survive the DataWriter.
         *
         * <p>For data to be replayed for late-joining reader(s) they should have {@link
         * DurabilityQosPolicy.Kind#TRANSIENT_LOCAL_DURABILITY_QOS}
         */
        TRANSIENT_LOCAL_DURABILITY_QOS,

        /**
         * For TRANSIENT, the service is only required to keep the data in memory and not in
         * permanent storage; but the data is not tied to the lifecycle of the DataWriter and will,
         * in general, survive it.
         */
        TRANSIENT_DURABILITY_QOS,

        /** Data is kept on permanent storage, so that they can outlive a system session. */
        PERSISTENT_DURABILITY_QOS
    }

    public int kind;

    public DurabilityQosPolicy() {
        this(Kind.VOLATILE_DURABILITY_QOS);
    }

    public DurabilityQosPolicy(Kind kind) {
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
        DurabilityQosPolicy other = (DurabilityQosPolicy) obj;
        return kind == other.kind;
    }

    @Override
    public String toString() {
        return Kind.values()[kind].toString();
    }
}
