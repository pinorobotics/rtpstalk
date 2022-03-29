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

import pinorobotics.rtpstalk.behavior.reader.RtpsReader;
import pinorobotics.rtpstalk.behavior.writer.RtpsWriter;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;

/**
 * {@link RtpsWriter} and {@link RtpsReader} by their nature are {@link RtpsMessage} publishers. To
 * keep them simple they don't keep track of all their subscribers. But because {@link RtpsMessage}
 * may have some fields which require explicit note about its consumer (like reader entity id) we
 * implement this class which is populated by {@link RtpsWriter} and {@link RtpsReader} and later is
 * used by their subscribers to instantiate {@link RtpsMessage} specific to their exact consumer.
 *
 * @author lambdaprime intid@protonmail.com
 */
public interface RtpsMessageBuilder {

    RtpsMessage build(EntityId readerEntiyId, EntityId writerEntityId);
}
