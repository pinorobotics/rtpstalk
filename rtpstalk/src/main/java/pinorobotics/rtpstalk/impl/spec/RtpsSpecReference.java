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
package pinorobotics.rtpstalk.impl.spec;

import java.lang.annotation.Repeatable;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;

/**
 * Tracks implementation with respect to actual RTPS specification document.
 *
 * @author lambdaprime intid@protonmail.com
 */
@Repeatable(RtpsSpecReferences.class)
public @interface RtpsSpecReference {

    ProtocolVersion.Predefined protocolVersion();

    String paragraph();

    String text() default "";

    String RTPS23 = "DDSI-RTPS/2.3";
}
