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
import java.util.List;
import java.util.Objects;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class HistoryQosPolicy implements HasStreamedFields {
    static final List<String> STREAMED_FIELDS = List.of("kind", "depth");

    public enum Kind {
        KEEP_LAST_HISTORY_QOS,
        KEEP_ALL_HISTORY_QOS
    }

    public int kind;
    public int depth;

    public HistoryQosPolicy() {
        this(Kind.KEEP_LAST_HISTORY_QOS, 1);
    }

    public HistoryQosPolicy(Kind kind, int depth) {
        this.kind = kind.ordinal();
        this.depth = depth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, depth);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        HistoryQosPolicy other = (HistoryQosPolicy) obj;
        return kind == other.kind && depth == other.depth;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("kind", Kind.values()[kind].toString());
        builder.append("depth", depth);
        return builder.toString();
    }
}
