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
package pinorobotics.rtpstalk.impl.spec.behavior.reader;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.RtpsDataPackager;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.walk.Result;
import pinorobotics.rtpstalk.impl.spec.messages.walk.RtpsSubmessageVisitor;
import pinorobotics.rtpstalk.impl.spec.messages.walk.RtpsSubmessagesWalker;
import pinorobotics.rtpstalk.impl.spec.structure.RtpsEntity;
import pinorobotics.rtpstalk.impl.spec.structure.history.CacheChange;
import pinorobotics.rtpstalk.impl.spec.structure.history.HistoryCache;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiver;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * Stateless RTPS endpoint reader (best-effort reliability) which can be subscribed to {@link
 * RtpsMessageReceiver} to receive RTPS messages and process them.
 *
 * <p>Each reader can be subscribed to only one {@link RtpsMessageReceiver}. And to one {@link
 * RtpsMessageReceiver} can be subscribed multiple different readers (many endpoints to one
 * receiver).
 *
 * <pre>{@code
 * USER subscribes to:
 * - {@link RtpsReader} subscribes to:
 *  - {@link RtpsMessageReceiver} receives messages from single data channel
 *   - remote writerX endpoint1
 *   - remote writerX endpoint2
 *   - ...
 *
 * }</pre>
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsReader<D extends RtpsTalkMessage> extends SubmissionPublisher<D>
        implements RtpsEntity, Subscriber<RtpsMessage>, RtpsSubmessageVisitor {

    protected final XLogger logger;

    private HistoryCache<D> cache = new HistoryCache<>();
    private RtpsSubmessagesWalker walker = new RtpsSubmessagesWalker();
    private Guid guid;
    private RtpsSubmessageVisitor filterVisitor;
    private ReliabilityQosPolicy.Kind reliabilityKind;
    private Optional<Subscription> subscription = Optional.empty();
    private TracingToken tracingToken;
    private RtpsDataPackager<D> packager = new RtpsDataPackager<>();

    public RtpsReader(
            RtpsTalkConfiguration config,
            TracingToken token,
            Executor publisherExecutor,
            Guid readerGuid,
            ReliabilityQosPolicy.Kind reliabilityKind) {
        super(publisherExecutor, config.publisherMaxBufferSize());
        this.tracingToken = new TracingToken(token, readerGuid.entityId.toString());
        this.guid = readerGuid;
        this.reliabilityKind = reliabilityKind;
        filterVisitor = new FilterByEntityIdRtpsSubmessageVisitor(readerGuid.entityId, this);
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    /** Contains the history of CacheChange changes for this RTPS Reader. */
    public HistoryCache<D> getReaderCache() {
        return cache;
    }

    @Override
    public Guid getGuid() {
        return guid;
    }

    public ReliabilityQosPolicy.Kind getReliabilityKind() {
        return reliabilityKind;
    }

    @Override
    public Result onData(GuidPrefix guidPrefix, Data d) {
        logger.fine("Received data {0}", d);
        var writerGuid = new Guid(guidPrefix, d.writerId);
        packager.extractMessage(d)
                .ifPresent(
                        message -> {
                            addChange(new CacheChange<>(writerGuid, d.writerSN.value, message));
                            if (!d.inlineQos.isEmpty()) processInlineQos(writerGuid, d.inlineQos);
                        });
        return Result.CONTINUE;
    }

    protected boolean addChange(CacheChange<D> cacheChange) {
        logger.entering("addChange");
        if (isClosed()) {
            logger.fine("Reader is closed, ignoring the change");
            return false;
        }
        var isAdded = cache.addChange(cacheChange);
        if (isAdded) {
            logger.fine("Submitting new change to subscribers");
            submit(cacheChange.getDataValue());
        }
        logger.exiting("addChange");
        return isAdded;
    }

    protected void process(RtpsMessage message) {
        walker.walk(message, filterVisitor);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        Preconditions.notNull(subscription, "Already subscribed");
        this.subscription = Optional.of(subscription);
        subscription.request(1);
    }

    @Override
    public void onNext(RtpsMessage message) {
        try {
            process(message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            subscription.get().request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        logger.severe(throwable);
        throw new UnsupportedOperationException();
    }

    @Override
    public void onComplete() {}

    protected void processInlineQos(Guid writer, ParameterList inlineQos) {
        // empty
    }

    @Override
    public String toString() {
        return tracingToken.toString();
    }

    public TracingToken getTracingToken() {
        return tracingToken;
    }

    @Override
    public void close() {
        subscription.ifPresent(Subscription::cancel);
        super.close();
        logger.fine("Closed");
    }
}
