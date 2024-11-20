/*
 * Copyright 2023 pinorobotics
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
package pinorobotics.rtpstalk;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public record WriterSettings(boolean pushMode) {

    /** Default pushMode */
    public static final boolean DEFAULT_PUSH_MODE = false;

    public WriterSettings() {
        this(DEFAULT_PUSH_MODE);
    }

    /**
     * Configures the mode in which the Writer operates. If pushMode==true, then the Writer will
     * push changes to the reader. If pushMode==false, changes will only be announced via heartbeats
     * and only be sent as response to the request of a reader.
     *
     * <p>When pushMode enabled it may produce more network traffic between the RTPS participants.
     * Due to concurrent nature of published messages and heartbeats (see 8.4.1.1 {@link
     * RtpsSpecReference#RTPS23}) it is possible that message will be sent twice (through heartbeat
     * and by pushing it). This does not affect users as in both cases only one message will be
     * submitted to the {@link Subscriber}.
     *
     * <p>When pushMode disabled it increases latency between publishing message by the {@link
     * Publisher} and receiving it from {@link Subscriber}
     */
    @RtpsSpecReference(paragraph = "8.4.7.1", protocolVersion = Predefined.Version_2_3)
    public boolean pushMode() {
        return pushMode;
    }
}
