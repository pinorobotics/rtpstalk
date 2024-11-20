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
package pinorobotics.rtpstalk.impl.topics;

import id.xfunction.XObservable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.qos.WriterQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;

/**
 * RTPS topic with all available remote and local actors to it.
 *
 * <p>Actors can be topic readers and writers.
 *
 * <p>It is observable so it is possible to add listener to it and receive events when there is a
 * match between local and remote actors (means they both belong to same topic, but not necessary
 * have compatible {@link WriterQosPolicySet} and {@link ReaderQosPolicySet})
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class Topic<A> extends XObservable<TopicMatchEvent<A>> {

    private TopicId topicId;
    private List<RemoteActorDetails> remoteActors = new ArrayList<>();
    private List<A> localActors = new ArrayList<>();
    private EntityId localTopicEntityId;

    public Topic(TopicId topicId, EntityId readerEntityId) {
        this.topicId = topicId;
        this.localTopicEntityId = readerEntityId;
    }

    public void addRemoteActor(RemoteActorDetails remoteActor) {
        localActors.stream()
                .forEach(subscriber -> updateAll(new TopicMatchEvent<>(remoteActor, subscriber)));
        remoteActors.add(remoteActor);
    }

    public void addLocalActor(A localActor) {
        remoteActors.stream()
                .forEach(remoteActor -> updateAll(new TopicMatchEvent<>(remoteActor, localActor)));
        localActors.add(localActor);
    }

    public boolean isMatches(TopicId topicId) {
        return Objects.equals(this.topicId, topicId);
    }

    public TopicId getTopicId() {
        return topicId;
    }

    public boolean hasLocalActors() {
        return !localActors.isEmpty();
    }

    public EntityId getLocalTopicEntityId() {
        return localTopicEntityId;
    }

    @Override
    public String toString() {
        return topicId.toString();
    }
}
