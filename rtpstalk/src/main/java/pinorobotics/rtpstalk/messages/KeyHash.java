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
package pinorobotics.rtpstalk.messages;

import id.xfunction.XAsserts;
import id.xfunction.XByte;
import id.xfunction.XJsonStringBuilder;

public class KeyHash implements Sequence {

    public static final int SIZE = 16;

    public byte[] value = new byte[SIZE];

    public KeyHash() {}

    public KeyHash(int... value) {
        XAsserts.assertEquals(SIZE, value.length, "Value size is wrong");
        this.value = XByte.castToByteArray(value);
    }

    public KeyHash(Guid guid) {
        value = new byte[SIZE];
        System.arraycopy(guid.guidPrefix.value, 0, value, 0, guid.guidPrefix.value.length);
        System.arraycopy(
                guid.entityId.entityKey,
                0,
                value,
                guid.guidPrefix.value.length,
                guid.entityId.entityKey.length);
        value[value.length - 1] = guid.entityId.entityKind;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", value);
        return builder.toString();
    }
}
