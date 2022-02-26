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
package pinorobotics.rtpstalk.messages.submessages;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.messages.submessages.elements.SubmessageElement;

public class SerializedPayload implements SubmessageElement {

    public SerializedPayloadHeader serializedPayloadHeader;

    public Payload payload;

    public SerializedPayload() {}

    public SerializedPayload(SerializedPayloadHeader serializedPayloadHeader, Payload payload) {
        this.serializedPayloadHeader = serializedPayloadHeader;
        this.payload = payload;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("serializedPayloadHeader", serializedPayloadHeader);
        builder.append("payload", payload);
        return builder.toString();
    }
}
