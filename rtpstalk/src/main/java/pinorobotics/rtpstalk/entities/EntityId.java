package pinorobotics.rtpstalk.entities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class EntityId {

	public static enum Predefined {
		ENTITYID_PARTICIPANT(new EntityId(new byte[]{00,00,01}, 0xc1)),
		ENTITYID_SEDP_BUILTIN_TOPICS_ANNOUNCER(new EntityId(new byte[]{00,00,02}, 0xc2)),
		ENTITYID_SEDP_BUILTIN_TOPICS_DETECTOR(new EntityId(new byte[]{00,00,02}, 0xc7)),
		ENTITYID_SEDP_BUILTIN_PUBLICATIONS_ANNOUNCER(new EntityId(new byte[]{00,00,03}, 0xc2)),
		ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR(new EntityId(new byte[]{00,00,03}, 0xc7)),
		ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER(new EntityId(new byte[]{00,00,04}, 0xc2)),
		ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR(new EntityId(new byte[]{00,00,04}, 0xc7)),
		ENTITYID_SPDP_BUILTIN_PARTICIPANT_ANNOUNCER(new EntityId(new byte[]{00,01,00}, 0xc2)),
		ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR(new EntityId(new byte[]{00,01,00}, 0xc7)),
		ENTITYID_P2P_BUILTIN_PARTICIPANT_MESSAGE_WRITER(new EntityId(new byte[]{00,02,00}, 0xc2)),
		ENTITYID_P2P_BUILTIN_PARTICIPANT_MESSAGE_READER(new EntityId(new byte[]{00,02,00}, 0xc7)),
		ENTITYID_UNKNOWN(new EntityId());
		
		private EntityId value;

		Predefined(EntityId value) {
			this.value = value;
		}
		
		public EntityId getValue() {
			return value;
		}
	}
	
	@Streamed
	public byte[] entityKey = new byte[3];

	@Streamed
	public byte entityKind;

	public EntityId() {
		// TODO Auto-generated constructor stub
	}
	
	public EntityId(byte[] entityKey, int entityKind) {
		this.entityKey = entityKey;
		this.entityKind = (byte) entityKind;
	}
	
	static Map<EntityId, Predefined> map = new HashMap<>();
	static {
		for (var t: Predefined.values()) map.put(t.value, t);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(entityKey);
		result = prime * result + Objects.hash(entityKind);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityId other = (EntityId) obj;
		return Arrays.equals(entityKey, other.entityKey) && entityKind == other.entityKind;
	}

	@Override
	public String toString() {
		var predefined = map.get(this);
		if (predefined != null) {
			return predefined.name();
		}
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("entityKey", Arrays.toString(entityKey));
		builder.append("entityKind", entityKind);
		return builder.toString();
	}
}
