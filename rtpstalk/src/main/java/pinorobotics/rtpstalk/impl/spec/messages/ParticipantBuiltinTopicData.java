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
package pinorobotics.rtpstalk.impl.spec.messages;

import id.xfunction.XJsonStringBuilder;
import java.util.List;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ParticipantBuiltinTopicData implements HasStreamedFields {
    static final List<String> STREAMED_FIELDS = List.of("key", "user_data");
    public BuiltinTopicKey key;

    public UserDataQosPolicy user_data;

    public ParticipantBuiltinTopicData() {}

    public ParticipantBuiltinTopicData(BuiltinTopicKey key, UserDataQosPolicy user_data) {
        this.key = key;
        this.user_data = user_data;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("key", key);
        builder.append("user_data", user_data);
        return builder.toString();
    }
}
