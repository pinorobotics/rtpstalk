package pinorobotics.rtpstalk.dto.submessages;

import id.xfunction.XJsonStringBuilder;
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
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("guidPrefix", guidPrefix);
		builder.append("entityId", entityId);
		return builder.toString();
	}
}
