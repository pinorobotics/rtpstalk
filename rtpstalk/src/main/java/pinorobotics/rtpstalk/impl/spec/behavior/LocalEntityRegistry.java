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
package pinorobotics.rtpstalk.impl.spec.behavior;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.spec.structure.RtpsEntity;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class LocalEntityRegistry<E extends RtpsEntity> {

    private XLogger logger;
    private Map<EntityId, E> entities = new ConcurrentHashMap<>();
    private Map<TopicId, EntityId> entityIds = new ConcurrentHashMap<>();
    private int entityIdCounter = 1;
    private final EnumSet<EntityKind> kinds = EnumSet.noneOf(EntityKind.class);

    public LocalEntityRegistry(TracingToken tracingToken, EntityKind... kinds) {
        Arrays.stream(kinds).forEach(this.kinds::add);
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    public synchronized void add(E entity) {
        var entityId = entity.getGuid().entityId;
        var entityKind = EntityKind.valueOf(entityId.entityKind());
        Preconditions.isTrue(
                kinds.contains(entityKind),
                "Entity kind missmatch: registry %s, new entity %s",
                kinds.toString(),
                entityKind);
        Preconditions.isTrue(
                !entities.containsKey(entityId), "Entity %s already present", entityId);
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

    public synchronized EntityId assignNewEntityId(TopicId topicId, EntityKind kind) {
        Preconditions.isTrue(
                kinds.contains(kind),
                "Entity kind missmatch: registry %s, new entity %s",
                kinds.toString(),
                kind);
        var entityId = entityIds.get(topicId);
        if (entityId == null) {
            entityId = new EntityId(entityIdCounter++, kind);
            logger.fine("Assigning new entity id {0} to the topic {1}", entityId, topicId);
            entityIds.put(topicId, entityId);
        }
        return entityId;
    }

    public EntityId assignEntityIdIfAbsent(TopicId topicId, EntityKind kind) {
        return findEntityId(topicId)
                .orElseGet(() -> assignNewEntityId(topicId, EntityKind.READER_NO_KEY));
    }

    public Optional<E> findEntity(TopicId topicId) {
        return findEntityId(topicId).flatMap(this::find);
    }

    public Collection<E> getEntities() {
        return entities.values();
    }
}
