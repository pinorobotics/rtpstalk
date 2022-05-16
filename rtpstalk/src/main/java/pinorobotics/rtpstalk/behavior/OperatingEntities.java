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
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.behavior.writer.StatefullRtpsWriter;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.userdata.DataWriter;

/** @author lambdaprime intid@protonmail.com */
public class OperatingEntities {

    private static final XLogger LOGGER = XLogger.getLogger(OperatingEntities.class);
    private Map<EntityId, StatefullRtpsWriter<?>> writers = new ConcurrentHashMap<>();
    private Map<EntityId, StatefullRtpsReader<?>> readers = new ConcurrentHashMap<>();
    private Map<String, DataWriter> topicNameToWriter = new ConcurrentHashMap<>();
    private Map<TopicId, EntityId> readerEntityIds = new ConcurrentHashMap<>();
    private int readerEntityIdCounter;

    public synchronized void add(EntityId entityId, StatefullRtpsWriter<?> writer) {
        Preconditions.isTrue(
                !writers.containsKey(entityId), "Writer " + entityId + " already present");
        writers.put(entityId, writer);
    }

    public synchronized void add(EntityId entityId, StatefullRtpsReader<?> reader) {
        Preconditions.isTrue(
                !readers.containsKey(entityId), "Reader " + entityId + " already present");
        readers.put(entityId, reader);
    }

    public Optional<StatefullRtpsWriter<?>> findStatefullWriter(EntityId entityId) {
        return Optional.ofNullable(writers.get(entityId));
    }

    public Optional<StatefullRtpsReader<?>> findStatefullReader(EntityId entityId) {
        return Optional.ofNullable(readers.get(entityId));
    }

    public void remove(EntityId entityId) {
        writers.remove(entityId);
    }

    public void add(String topic, DataWriter writer) {
        topicNameToWriter.put(topic, writer);
    }

    public Optional<DataWriter> findWriter(String topicName) {
        return Optional.ofNullable(topicNameToWriter.get(topicName));
    }

    public Optional<EntityId> findReaderEntityId(TopicId topicId) {
        return Optional.ofNullable(readerEntityIds.get(topicId));
    }

    public synchronized EntityId assignNewReaderEntityId(TopicId topicId) {
        var entityId = readerEntityIds.get(topicId);
        if (entityId == null) {
            entityId = new EntityId(readerEntityIdCounter++, EntityKind.READER_NO_KEY);
            LOGGER.fine("Assigning new reader entity id {0} to the topic {1}", entityId, topicId);
            readerEntityIds.put(topicId, entityId);
        }
        return entityId;
    }
}
