package pinorobotics.rtpstalk.entities;

import id.xfunction.XJsonStringBuilder;

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
