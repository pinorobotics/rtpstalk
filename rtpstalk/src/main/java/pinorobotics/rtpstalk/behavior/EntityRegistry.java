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

import id.xfunction.Preconditions;
import id.xfunction.logging.XLogger;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.structure.RtpsEntity;

/** @author lambdaprime intid@protonmail.com */
public class EntityRegistry<E extends RtpsEntity> {

    private static final XLogger LOGGER = XLogger.getLogger(EntityRegistry.class);
    private Map<EntityId, E> entities = new ConcurrentHashMap<>();
    private Map<TopicId, EntityId> entityIds = new ConcurrentHashMap<>();
    private int entityIdCounter;
    private String name;

    public EntityRegistry(String name) {
        this.name = name;
    }

    public synchronized void add(E entity) {
        var entityId = entity.getGuid().entityId;
        Preconditions.isTrue(
                !entities.containsKey(entityId), "Writer " + entityId + " already present");
        entities.put(entityId, entity);
    }

    public Optional<E> find(EntityId entityId) {
        return Optional.ofNullable(entities.get(entityId));
    }

    public void remove(EntityId entityId) {
        entities.remove(entityId);
    }

    public Optional<EntityId> findEntityId(TopicId topicId) {
        return Optional.ofNullable(entityIds.get(topicId));
    }

    public synchronized EntityId assignNewEntityId(TopicId topicId) {
        var entityId = entityIds.get(topicId);
        if (entityId == null) {
            entityId = new EntityId(entityIdCounter++, EntityKind.READER_NO_KEY);
            LOGGER.fine(
                    "Assigning new entity id {0} to the topic {1} in <{2}> registry",
                    entityId, topicId, name);
            entityIds.put(topicId, entityId);
        }
        return entityId;
    }
}
