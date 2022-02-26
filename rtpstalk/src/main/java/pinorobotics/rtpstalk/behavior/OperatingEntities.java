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

import id.xfunction.XAsserts;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import pinorobotics.rtpstalk.behavior.writer.StatefullRtpsWriter;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;

/** @author lambdaprime intid@protonmail.com */
public class OperatingEntities {

    private static final OperatingEntities INSTANCE = new OperatingEntities();
    private Map<EntityId, StatefullRtpsWriter<?>> writers = new ConcurrentHashMap<>();

    public static OperatingEntities getInstance() {
        return INSTANCE;
    }

    public void add(EntityId entityId, StatefullRtpsWriter<?> writer) {
        XAsserts.assertTrue(
                !writers.containsKey(entityId), "Writer " + entityId + " already present");
        writers.put(entityId, writer);
    }

    public Optional<StatefullRtpsWriter<?>> findStatefullWriter(EntityId entityId) {
        return Optional.ofNullable(writers.get(entityId));
    }

    public void remove(EntityId entityId) {
        writers.remove(entityId);
    }
}
