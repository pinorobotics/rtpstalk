package pinorobotics.rtpstalk.entities;

import java.util.HashMap;
import java.util.Map;

import id.kineticstreamer.annotations.Streamed;

public class SubmessageKind {

	public static enum Value {
		PAD(0x01, InfoTimestamp.class), /* Pad */
		ACKNACK(0x06, InfoTimestamp.class), /* AckNack */
		HEARTBEAT(0x07, InfoTimestamp.class), /* Heartbeat */
		GAP(0x08, InfoTimestamp.class), /* Gap */
		INFO_TS(0x09, InfoTimestamp.class), /* InfoTimestamp */
		INFO_SRC(0x0c, InfoTimestamp.class), /* InfoSource */
		INFO_REPLY_IP4(0x0d, InfoTimestamp.class), /* InfoReplyIp4 */
		INFO_DST(0x0e, InfoTimestamp.class), /* InfoDestination */
		INFO_REPLY(0x0f, InfoTimestamp.class), /* InfoReply */
		NACK_FRAG(0x12, InfoTimestamp.class), /* NackFrag */
		HEARTBEAT_FRAG(0x13, InfoTimestamp.class), /* HeartbeatFrag */
		DATA(0x15, Data.class), /* Data */
		DATA_FRAG(0x16, InfoTimestamp.class); /* DataFrag */
		
		public byte value;
		private Class<? extends SubmessageElement> messageClass;
		
		Value(int i, Class<? extends SubmessageElement> messageClass) {
			this.messageClass = messageClass;
			this.value = (byte) i;
		}
	}
	
	static Map<Byte, Value> map = new HashMap<>();
	
	static {
		for (var t: Value.values()) map.put(t.value, t);
	}
	
	@Streamed
	public byte value;

	public Class<? extends SubmessageElement> getSubmessageClass() {
		return map.get(value).messageClass;
	}
	
	public Value getValue() {
		Value val = map.get(value);
		if (val == null) {
			throw new RuntimeException("Submessage kind " + value + " is unknown");
		}
		return val;
	}

	@Override
	public String toString() {
		return getValue().name();
	}
	
}
