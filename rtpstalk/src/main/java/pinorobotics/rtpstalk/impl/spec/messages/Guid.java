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
import java.util.Objects;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class Guid {

    public GuidPrefix guidPrefix;

    public EntityId entityId;

    public Guid() {}

    public Guid(GuidPrefix guidPrefix, EntityId entityId) {
        this.guidPrefix = guidPrefix;
        this.entityId = entityId;
    }

    public Guid(GuidPrefix guidPrefix, Predefined predefinedEntityId) {
        this(guidPrefix, predefinedEntityId.getValue());
    }

    public Guid(byte[] guidPrefix, EntityId entityId) {
        this(new GuidPrefix(guidPrefix), entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, guidPrefix);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Guid other = (Guid) obj;
        return Objects.equals(entityId, other.entityId)
                && Objects.equals(guidPrefix, other.guidPrefix);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("guidPrefix", guidPrefix);
        builder.append("entityId", entityId);
        return builder.toString();
    }
}
