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
package pinorobotics.rtpstalk.impl.spec.behavior;

import id.xfunction.logging.TracingToken;
import pinorobotics.rtpstalk.impl.spec.behavior.reader.StatefullReliableRtpsReader;
import pinorobotics.rtpstalk.impl.spec.behavior.writer.StatefullReliableRtpsWriter;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class LocalOperatingEntities {

    private LocalEntityRegistry<StatefullReliableRtpsWriter<?>> writers;
    private LocalEntityRegistry<StatefullReliableRtpsReader<?>> readers;

    public LocalOperatingEntities(TracingToken tracingToken) {
        writers =
                new LocalEntityRegistry<>(
                        new TracingToken(tracingToken, "WRITERS"),
                        EntityKind.WRITER_NO_KEY,
                        EntityKind.BUILTIN_WRITER);
        readers =
                new LocalEntityRegistry<>(
                        new TracingToken(tracingToken, "READERS"),
                        EntityKind.READER_NO_KEY,
                        EntityKind.BUILTIN_READER);
    }

    public LocalEntityRegistry<StatefullReliableRtpsReader<?>> getLocalReaders() {
        return readers;
    }

    public LocalEntityRegistry<StatefullReliableRtpsWriter<?>> getLocalWriters() {
        return writers;
    }
}
