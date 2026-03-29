/*
 * Copyright 2026 pinorobotics
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
package pinorobotics.rtpstalk.impl;

import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.TopicDiscoveryInfo;
import pinorobotics.rtpstalk.impl.actors.RemoteActorDetails;
import pinorobotics.rtpstalk.impl.qos.QosPolicyTransformer;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DiscoveryInfoPublisher implements AutoCloseable {

    private static final QosPolicyTransformer qosTransformer = new QosPolicyTransformer();
    private XLogger logger;
    private SubmissionPublisher<TopicDiscoveryInfo> submissionPublisher;

    public DiscoveryInfoPublisher(
            RtpsTalkConfiguration config, TracingToken tracingToken, Executor publisherExecutor) {
        submissionPublisher =
                new SubmissionPublisher<>(publisherExecutor, config.publisherMaxBufferSize());
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    public void submit(RemoteActorDetails remoteActor) {
        var info =
                new TopicDiscoveryInfo(
                        remoteActor.topicId().name(),
                        remoteActor.topicId().type(),
                        remoteActor.isPublisher(),
                        remoteActor.endpointGuid().array(),
                        qosTransformer.convert(remoteActor.reliabilityKind()),
                        qosTransformer.convert(remoteActor.durabilityKind()));
        logger.fine("Submit new disovery information {0}", info);
        submissionPublisher.submit(info);
        logger.fine("Submitted");
    }

    public void subscribe(Subscriber<TopicDiscoveryInfo> subscriber) {
        logger.fine("Subscribe new subscriber");
        submissionPublisher.subscribe(subscriber);
    }

    @Override
    public void close() {
        logger.fine("Close");
        submissionPublisher.close();
    }
}
