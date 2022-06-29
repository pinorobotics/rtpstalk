/*
 * Copyright 2022 rtpstalk project
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
package pinorobotics.rtpstalk.impl.spec.messages;

import id.xfunction.Preconditions;
import id.xfunction.XByte;
import id.xfunction.XJsonStringBuilder;
import java.nio.ByteBuffer;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class KeyHash implements Sequence {

    public static final int SIZE = 16;

    public byte[] value = new byte[SIZE];

    public KeyHash() {}

    public KeyHash(int... value) {
        Preconditions.equals(SIZE, value.length, "Value size is wrong");
        this.value = XByte.castToByteArray(value);
    }

    public KeyHash(Guid guid) {
        value = new byte[SIZE];
        var buf = ByteBuffer.wrap(value);
        buf.put(guid.guidPrefix.value);
        buf.putInt(guid.entityId.value);
    }

    public Guid asGuid() {
        var guidPrefix = new GuidPrefix();
        for (int i = 0; i < guidPrefix.value.length; i++) {
            guidPrefix.value[i] = value[i];
        }
        var pos = guidPrefix.value.length;
        var entityKey = value[pos++] << 16;
        entityKey |= value[pos++] << 8;
        entityKey |= value[pos++];
        return new Guid(guidPrefix, new EntityId(entityKey, value[pos]));
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", XByte.toHex(value));
        return builder.toString();
    }
}
