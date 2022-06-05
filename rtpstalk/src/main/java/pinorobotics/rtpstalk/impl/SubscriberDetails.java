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
package pinorobotics.rtpstalk.impl;

import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.impl.qos.SubscriberQosPolicy;
import pinorobotics.rtpstalk.messages.submessages.RawData;

/** @author lambdaprime intid@protonmail.com */
public record SubscriberDetails(
        TopicId topicId, SubscriberQosPolicy qosPolicy, Subscriber<RawData> subscriber) {

    public SubscriberDetails(TopicId topicId, Subscriber<RawData> subscriber) {
        this(topicId, new SubscriberQosPolicy.Builder().build(), subscriber);
    }
}
