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
package pinorobotics.rtpstalk.impl.spec.messages.submessages;

import id.xfunction.XJson;
import id.xfunction.XUtils;
import java.nio.ByteBuffer;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RepresentationIdentifier.Predefined;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RawData implements Payload {

    public byte[] data;

    public RawData() {}

    public RawData(byte[] data) {
        this.data = data;
    }

    public RawData(ByteBuffer data) {
        this.data = data.array();
    }

    public ByteBuffer getData() {
        return ByteBuffer.wrap(data);
    }

    @Override
    public Predefined getRepresentationIdentifier() {
        return RepresentationIdentifier.Predefined.CDR_LE;
    }

    @Override
    public String toString() {
        try {
            return XJson.asString("size", data.length, "md5", XUtils.md5Sum(data));
        } catch (Exception e) {
            return XJson.asString("size", data.length);
        }
    }

    @Override
    public boolean isEmpty() {
        return data.length == 0;
    }
}
