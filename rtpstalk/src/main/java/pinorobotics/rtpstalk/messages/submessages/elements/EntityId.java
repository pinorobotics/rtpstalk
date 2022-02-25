package pinorobotics.rtpstalk.messages.submessages.elements;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static pinorobotics.rtpstalk.messages.submessages.elements.EntityKind.*;

import id.xfunction.XByte;

public class EntityId {

    public static final int SIZE = 3;

    public static enum Predefined {
        ENTITYID_PARTICIPANT(new EntityId(new byte[] { 00, 00, 01 }, BUILTIN_PARTICIPANT)),
        ENTITYID_SEDP_BUILTIN_TOPICS_ANNOUNCER(new EntityId(new byte[] { 00, 00, 02 }, BUILTIN_WRITER)),
        ENTITYID_SEDP_BUILTIN_TOPICS_DETECTOR(new EntityId(new byte[] { 00, 00, 02 }, BUILTIN_READER)),

        /**
         * Publication endpoints allow topic publisher to announce what topics it has
         * and subscribers to detect those announcements.
         */
        ENTITYID_SEDP_BUILTIN_PUBLICATIONS_ANNOUNCER(
                new EntityId(new byte[] { 00, 00, 03 }, BUILTIN_WRITER)),
        ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR(new EntityId(new byte[] { 00, 00, 03 }, BUILTIN_READER)),

        /**
         * Once subscriber detected that there is a new topic available (through
         * publication endpoints) it can subscribe to it. To do that subscriber needs to
         * announce publisher about its intention to subscribe to the topic using
         * subscription endpoints.
         */
        ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER(
                new EntityId(new byte[] { 00, 00, 04 }, BUILTIN_WRITER)),
        ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR(
                new EntityId(new byte[] { 00, 00, 04 }, BUILTIN_READER)),

        ENTITYID_SPDP_BUILTIN_PARTICIPANT_ANNOUNCER(new EntityId(new byte[] { 00, 01, 00 }, BUILTIN_WRITER)),
        ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR(new EntityId(new byte[] { 00, 01, 00 }, BUILTIN_READER)),
        ENTITYID_P2P_BUILTIN_PARTICIPANT_MESSAGE_WRITER(
                new EntityId(new byte[] { 00, 02, 00 }, BUILTIN_WRITER)),
        ENTITYID_P2P_BUILTIN_PARTICIPANT_MESSAGE_READER(
                new EntityId(new byte[] { 00, 02, 00 }, BUILTIN_READER)),
        ENTITYID_UNKNOWN(new EntityId());

        static final Map<EntityId, Predefined> MAP = Arrays.stream(Predefined.values())
                .collect(Collectors.toMap(k -> k.value, v -> v));
        private EntityId value;

        Predefined(EntityId value) {
            this.value = value;
        }

        public EntityId getValue() {
            return value;
        }
    }

    public byte[] entityKey = new byte[SIZE];

    public byte entityKind;

    public EntityId() {
    }

    public EntityId(byte[] entityKey, EntityKind entityKind) {
        this.entityKey = entityKey;
        this.entityKind = (byte) entityKind.getValue();
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
        var predefined = Predefined.MAP.get(this);
        if (predefined != null) {
            return predefined.name();
        }
        return XByte.toHex(entityKey) + "-" + XByte.toHex(entityKind);
    }
}
