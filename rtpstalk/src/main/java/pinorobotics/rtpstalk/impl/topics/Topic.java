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
package pinorobotics.rtpstalk.impl.topics;

import id.xfunction.XObservable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import pinorobotics.rtpstalk.impl.SubscriberDetails;
import pinorobotics.rtpstalk.impl.TopicId;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;

/**
 * RTPS topic with all available remote publishers and local subscribers to it.
 *
 * <p>It is observable so it is possible to add listener to it and receive different events.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class Topic extends XObservable<SubscribeEvent> {

    private record TopicPublisher(Locator writerUnicastLocator, Guid endpointGuid) {}

    private TopicId topicId;
    private List<TopicPublisher> discoveredPublishers = new ArrayList<>();
    private List<SubscriberDetails> applicationSubscribers = new ArrayList<>();

    public Topic(TopicId topicId) {
        this.topicId = topicId;
    }

    public void addPublisher(Locator writerUnicastLocator, Guid endpointGuid) {
        applicationSubscribers.stream()
                .forEach(
                        subscriber ->
                                updateAll(
                                        new SubscribeEvent(
                                                writerUnicastLocator, endpointGuid, subscriber)));
        discoveredPublishers.add(new TopicPublisher(writerUnicastLocator, endpointGuid));
    }

    public void addSubscriber(SubscriberDetails subscriber) {
        discoveredPublishers.stream()
                .forEach(
                        publisher ->
                                updateAll(
                                        new SubscribeEvent(
                                                publisher.writerUnicastLocator,
                                                publisher.endpointGuid,
                                                subscriber)));
        applicationSubscribers.add(subscriber);
    }

    public boolean isMatches(TopicId topicId) {
        return Objects.equals(this.topicId, topicId);
    }

    public TopicId getTopicId() {
        return topicId;
    }
}
