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
package pinorobotics.rtpstalk.messages;

import java.util.Optional;

/**
 * Base interface for all kinds of messages which can be sent with {@link
 * pinorobotics.rtpstalk.RtpsTalkClient}
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public interface RtpsTalkMessage {

    /** RTPS inline QoS to be included with a message (can be empty) */
    Optional<Parameters> userInlineQos();
}
