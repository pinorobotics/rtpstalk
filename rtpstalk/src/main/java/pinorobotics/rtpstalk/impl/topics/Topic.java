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
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.submessages.RawData;

/**
 * RTPS topic with all available remote publishers and local subscribers to it
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class Topic extends XObservable<SubscribeEvent> {

    private record TopicPublisher(Locator writerUnicastLocator, Guid endpointGuid) {}

    private String name;
    private String type;
    private List<TopicPublisher> discoveredPublishers = new ArrayList<>();
    private List<Subscriber<RawData>> applicationSubscribers = new ArrayList<>();

    public Topic(String name, String type) {
        this.name = name;
        this.type = type;
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

    public void addSubscriber(Subscriber<RawData> subscriber) {
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

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isMatches(String name, String type) {
        return Objects.equals(this.name, name) && Objects.equals(this.type, type);
    }
}
