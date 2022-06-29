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

import java.util.concurrent.Flow.Publisher;
import pinorobotics.rtpstalk.impl.qos.PublisherQosPolicy;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;

/**
 * @author lambdaprime intid@protonmail.com
 */
public record PublisherDetails(
        TopicId topicId, PublisherQosPolicy qosPolicy, Publisher<RtpsTalkDataMessage> publisher) {

    public PublisherDetails(TopicId topicId, Publisher<RtpsTalkDataMessage> subscriber) {
        this(topicId, new PublisherQosPolicy.Builder().build(), subscriber);
    }
}
