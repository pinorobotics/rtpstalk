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

import id.xfunction.Preconditions;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public enum EntityKind {
    BUILTIN_UNKNOWN(0xc0, true),
    BUILTIN_PARTICIPANT(0xc1, true),
    BUILTIN_WRITER(0xc2, true),
    BUILTIN_WRITER_NO_KEY(0xc3, true),
    BUILTIN_READER(0xc7, true),
    BUILTIN_READER_NO_KEY(0xc4, true),
    BUILTIN_WRITER_GROUP(0xc8, true),
    BUILTIN_READER_GROUP(0xc9, true),

    UNKNOWN(0x00, true),
    WRITER(0x02, false),

    /**
     * Enumeration used to distinguish whether a Topic has defined some fields within to be used as
     * the key that identifies data-instances within the Topic.
     */
    WRITER_NO_KEY(0x03, false),
    READER_NO_KEY(0x04, false),
    READER(0x07, false),
    WRITER_GROUP(0x08, true),
    READER_GROUP(0x09, true);

    private static final Map<Byte, EntityKind> MAP =
            Arrays.stream(EntityKind.values()).collect(Collectors.toMap(k -> k.value, v -> v));
    private byte value;
    private boolean isBuiltin;

    private EntityKind(int value, boolean isBuiltin) {
        this.isBuiltin = isBuiltin;
        this.value = (byte) value;
    }

    public byte getValue() {
        return value;
    }

    public boolean isBuiltin() {
        return isBuiltin;
    }

    public static EntityKind valueOf(byte b) {
        var kind = MAP.get(b);
        Preconditions.notNull(kind, "Value %s does not exist", b);
        return kind;
    }
}
