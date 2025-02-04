/*
 * Copyright 2022 pinorobotics
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
package pinorobotics.rtpstalk.impl.spec.messages.submessages.elements;

import static pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind.BUILTIN_PARTICIPANT;
import static pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind.BUILTIN_READER;
import static pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind.BUILTIN_WRITER;

import id.xfunction.XByte;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;

/**
 * Uniquely identifies the Entity within the Participant
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class EntityId implements HasStreamedFields {
    public static final int SIZE = Integer.BYTES;

    public static enum Predefined {
        ENTITYID_PARTICIPANT(new EntityId(0x000001, BUILTIN_PARTICIPANT)),
        ENTITYID_SEDP_BUILTIN_TOPICS_ANNOUNCER(new EntityId(0x000002, BUILTIN_WRITER)),
        ENTITYID_SEDP_BUILTIN_TOPICS_DETECTOR(new EntityId(0x000002, BUILTIN_READER)),

        /**
         * Publication endpoints allow topic publisher to announce what topics it has and
         * subscribers to detect those announcements.
         */
        ENTITYID_SEDP_BUILTIN_PUBLICATIONS_ANNOUNCER(new EntityId(0x000003, BUILTIN_WRITER)),
        ENTITYID_SEDP_BUILTIN_PUBLICATIONS_DETECTOR(new EntityId(0x000003, BUILTIN_READER)),

        /**
         * Once subscriber detected that there is a new topic available (through publication
         * endpoints) it can subscribe to it. To do that subscriber needs to announce publisher
         * about its intention to subscribe to the topic using subscription endpoints.
         */
        ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_ANNOUNCER(new EntityId(0x000004, BUILTIN_WRITER)),
        ENTITYID_SEDP_BUILTIN_SUBSCRIPTIONS_DETECTOR(new EntityId(0x000004, BUILTIN_READER)),

        ENTITYID_SPDP_BUILTIN_PARTICIPANT_ANNOUNCER(new EntityId(0x000100, BUILTIN_WRITER)),
        ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR(new EntityId(0x000100, BUILTIN_READER)),
        ENTITYID_P2P_BUILTIN_PARTICIPANT_MESSAGE_WRITER(new EntityId(0x000200, BUILTIN_WRITER)),
        ENTITYID_P2P_BUILTIN_PARTICIPANT_MESSAGE_READER(new EntityId(0x000200, BUILTIN_READER)),
        ENTITYID_UNKNOWN(new EntityId());

        static final Map<EntityId, Predefined> MAP =
                Arrays.stream(Predefined.values()).collect(Collectors.toMap(k -> k.value, v -> v));
        private EntityId value;

        Predefined(EntityId value) {
            this.value = value;
        }

        public EntityId getValue() {
            return value;
        }
    }

    public int value;

    public EntityId() {}

    public EntityId(int entityKey, EntityKind entityKind) {
        this(entityKey, entityKind.getValue());
    }

    public EntityId(int entityKey, byte entityKind) {
        this((entityKey << 8) | Byte.toUnsignedInt(entityKind));
    }

    public EntityId(int value) {
        this.value = value;
    }

    public EntityId(String entityId) {
        this(HexFormat.fromHexDigits(entityId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        EntityId other = (EntityId) obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        var predefined = Predefined.MAP.get(this);
        if (predefined != null) {
            return predefined.name();
        }
        return XByte.toHex(value);
    }

    public byte entityKind() {
        return (byte) (value & 0x000000ff);
    }

    public byte entityKey() {
        return (byte) (value >> 8);
    }

    public boolean isBuiltin() {
        return EntityKind.valueOf(entityKind()).isBuiltin();
    }
}
