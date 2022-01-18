package pinorobotics.rtpstalk.dto.submessages;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import id.xfunction.XJsonStringBuilder;

public class SubmessageKind {

    public static enum Predefined {

        /** Pad */
        PAD(new SubmessageKind(0x01), InfoTimestamp.class),

        /** AckNack */
        ACKNACK(new SubmessageKind(0x06), InfoTimestamp.class),

        /** Heartbeat */
        HEARTBEAT(new SubmessageKind(0x07), Heartbeat.class),

        /** Gap */
        GAP(new SubmessageKind(0x08), InfoTimestamp.class),

        /** InfoTimestamp */
        INFO_TS(new SubmessageKind(0x09), InfoTimestamp.class),

        /** InfoSource */
        INFO_SRC(new SubmessageKind(0x0c), InfoTimestamp.class),

        /** InfoReplyIp4 */
        INFO_REPLY_IP4(new SubmessageKind(0x0d), InfoTimestamp.class),

        /** InfoDestination */
        INFO_DST(new SubmessageKind(0x0e), InfoDestination.class),

        /** InfoReply */
        INFO_REPLY(new SubmessageKind(0x0f), InfoTimestamp.class),

        /** NackFrag */
        NACK_FRAG(new SubmessageKind(0x12), InfoTimestamp.class),

        /** HeartbeatFrag */
        HEARTBEAT_FRAG(new SubmessageKind(0x13), InfoTimestamp.class),

        /** Data */
        DATA(new SubmessageKind(0x15), Data.class),

        /** DataFrag */
        DATA_FRAG(new SubmessageKind(0x16), InfoTimestamp.class);

        static final Map<SubmessageKind, Predefined> MAP = Arrays.stream(Predefined.values())
                .collect(Collectors.toMap(k -> k.value, v -> v));
        private SubmessageKind value;
        private Class<? extends Submessage> messageClass;

        Predefined(SubmessageKind value, Class<? extends Submessage> messageClass) {
            this.value = value;
            this.messageClass = messageClass;
        }

        public SubmessageKind getValue() {
            return value;
        }
    }

    public byte value;

    public SubmessageKind() {

    }

    public SubmessageKind(int value) {
        this.value = (byte) value;
    }

    public Optional<Class<? extends Submessage>> getSubmessageClass() {
        return Optional.ofNullable(Predefined.MAP.get(this))
                .map(val -> val.messageClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
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
        return value == other.value;
    }

    @Override
    public String toString() {
        var predefined = Predefined.MAP.get(this);
        if (predefined != null) {
            return predefined.name();
        }
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", value);
        return builder.toString();
    }

}
