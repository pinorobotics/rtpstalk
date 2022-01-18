package pinorobotics.rtpstalk.dto;

import java.util.Objects;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.dto.submessages.elements.EntityId;
import pinorobotics.rtpstalk.dto.submessages.elements.GuidPrefix;

public class Guid {

    public GuidPrefix guidPrefix;

    public EntityId entityId;

    public Guid() {

    }

    public Guid(GuidPrefix guidPrefix, EntityId entityId) {
        this.guidPrefix = guidPrefix;
        this.entityId = entityId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, guidPrefix);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Guid other = (Guid) obj;
        return Objects.equals(entityId, other.entityId) && Objects.equals(guidPrefix, other.guidPrefix);
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("guidPrefix", guidPrefix);
        builder.append("entityId", entityId);
        return builder.toString();
    }
}
