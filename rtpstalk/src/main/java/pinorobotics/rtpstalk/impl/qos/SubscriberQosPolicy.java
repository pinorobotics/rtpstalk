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
package pinorobotics.rtpstalk.impl.qos;

import id.xfunction.XJsonStringBuilder;

/**
 * @author lambdaprime intid@protonmail.com
 */
public record SubscriberQosPolicy(ReliabilityKind reliabilityKind) {

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("reliabilityKind", reliabilityKind);
        return builder.toString();
    }

    public static class Builder {

        public static final ReliabilityKind DEFAULT_RELIABILITY_KIND = ReliabilityKind.RELIABLE;

        private ReliabilityKind reliabilityKind = DEFAULT_RELIABILITY_KIND;

        public Builder reliabilityKind(ReliabilityKind reliabilityKind) {
            this.reliabilityKind = reliabilityKind;
            return this;
        }

        public SubscriberQosPolicy build() {
            return new SubscriberQosPolicy(reliabilityKind);
        }
    }
}
