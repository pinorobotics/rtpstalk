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
package pinorobotics.rtpstalk.behavior;

import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.behavior.writer.StatefullRtpsWriter;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityKind;

/** @author lambdaprime intid@protonmail.com */
public class OperatingEntities {

    private EntityRegistry<StatefullRtpsWriter<?>> writers =
            new EntityRegistry<>(
                    new TracingToken("WRITERS"),
                    EntityKind.WRITER_NO_KEY,
                    EntityKind.BUILTIN_WRITER);
    private EntityRegistry<StatefullRtpsReader<?>> readers =
            new EntityRegistry<>(
                    new TracingToken("READERS"),
                    EntityKind.READER_NO_KEY,
                    EntityKind.BUILTIN_READER);

    public EntityRegistry<StatefullRtpsReader<?>> getReaders() {
        return readers;
    }

    public EntityRegistry<StatefullRtpsWriter<?>> getWriters() {
        return writers;
    }
}
