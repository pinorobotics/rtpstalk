package pinorobotics.rtpstalk.entities;

import java.util.Objects;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class Guid {
	
	@Streamed
	public GuidPrefix guidPrefix;
	
	@Streamed
	public EntityId entityId;
	
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
