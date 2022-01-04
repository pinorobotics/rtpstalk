package pinorobotics.rtpstalk.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class SubmessageKind {

	public static enum Predefined {
		PAD(new SubmessageKind(0x01, InfoTimestamp.class)), /* Pad */
		ACKNACK(new SubmessageKind(0x06, InfoTimestamp.class)), /* AckNack */
		HEARTBEAT(new SubmessageKind(0x07, InfoTimestamp.class)), /* Heartbeat */
		GAP(new SubmessageKind(0x08, InfoTimestamp.class)), /* Gap */
		INFO_TS(new SubmessageKind(0x09, InfoTimestamp.class)), /* InfoTimestamp */
		INFO_SRC(new SubmessageKind(0x0c, InfoTimestamp.class)), /* InfoSource */
		INFO_REPLY_IP4(new SubmessageKind(0x0d, InfoTimestamp.class)), /* InfoReplyIp4 */
		INFO_DST(new SubmessageKind(0x0e, InfoTimestamp.class)), /* InfoDestination */
		INFO_REPLY(new SubmessageKind(0x0f, InfoTimestamp.class)), /* InfoReply */
		NACK_FRAG(new SubmessageKind(0x12, InfoTimestamp.class)), /* NackFrag */
		HEARTBEAT_FRAG(new SubmessageKind(0x13, InfoTimestamp.class)), /* HeartbeatFrag */
		DATA(new SubmessageKind(0x15, Data.class)), /* Data */
		DATA_FRAG(new SubmessageKind(0x16, InfoTimestamp.class)); /* DataFrag */
		
		private SubmessageKind value;

		Predefined(SubmessageKind value) {
			this.value = value;
		}
		
		public SubmessageKind getValue() {
			return value;
		}
	}
	
	static Map<SubmessageKind, Predefined> map = new HashMap<>();
	static {
		for (var t: Predefined.values()) map.put(t.value, t);
	}
	
	@Streamed
	public byte value;
	
	private Class<? extends SubmessageElement> messageClass;

	public SubmessageKind() {
		// TODO Auto-generated constructor stub
	}
	
	public SubmessageKind(int value, Class<? extends SubmessageElement> messageClass) {
		this.messageClass = messageClass;
		this.value = (byte) value;
	}

	public Class<? extends SubmessageElement> getSubmessageClass() {
		return messageClass;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(messageClass, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubmessageKind other = (SubmessageKind) obj;
		return Objects.equals(messageClass, other.messageClass) && value == other.value;
	}

	@Override
	public String toString() {
		var predefined = map.get(this);
		if (predefined != null) {
			return predefined.name();
		}
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("value", value);
		return builder.toString();
	}
	
}
