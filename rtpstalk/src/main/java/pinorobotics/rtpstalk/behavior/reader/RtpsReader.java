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
package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.Preconditions;
import id.xfunction.logging.XLogger;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.ReliabilityKind;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.walk.Result;
import pinorobotics.rtpstalk.messages.walk.RtpsSubmessageVisitor;
import pinorobotics.rtpstalk.messages.walk.RtpsSubmessagesWalker;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.structure.HistoryCache;
import pinorobotics.rtpstalk.structure.RtpsEntity;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;

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
 *   - remote writer endpoint1
 *   - remote writer endpoint2
 *   - ...
 *
 * }</pre>
 */
public class RtpsReader<D extends Payload> extends SubmissionPublisher<D>
        implements RtpsEntity, Subscriber<RtpsMessage>, RtpsSubmessageVisitor {

    protected final XLogger logger;

    private HistoryCache<D> cache = new HistoryCache<>();
    private RtpsSubmessagesWalker walker = new RtpsSubmessagesWalker();
    private Guid guid;
    private RtpsSubmessageVisitor filterVisitor;
    private ReliabilityKind reliabilityKind;
    private Subscription subscription;
    private TracingToken tracingToken;

    public RtpsReader(
            TracingToken token,
            Guid readerGuid,
            ReliabilityKind reliabilityKind,
            Executor executor,
            int maxBufferCapacity) {
        super(executor, maxBufferCapacity);
        this.tracingToken = new TracingToken(token, readerGuid.entityId.toString());
        this.guid = readerGuid;
        this.reliabilityKind = reliabilityKind;
        filterVisitor = new FilterByEntityIdRtpsSubmessageVisitor(readerGuid.entityId, this);
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
    }

    /** Contains the history of CacheChange changes for this RTPS Reader. */
    public HistoryCache<D> getReaderCache() {
        return cache;
    }

    @Override
    public Guid getGuid() {
        return guid;
    }

    public ReliabilityKind getReliabilityKind() {
        return reliabilityKind;
    }

    @Override
    public Result onData(GuidPrefix guidPrefix, Data d) {
        logger.fine("Received data {0}", d);
        var writerGuid = new Guid(guidPrefix, d.writerId);
        if (d.serializedPayload != null) {
            addChange(
                    new CacheChange<>(
                            writerGuid, d.writerSN.value, (D) d.serializedPayload.payload));
        }
        d.inlineQos
                .map(ParameterList::getParameters)
                .ifPresent(pl -> processInlineQos(writerGuid, pl));
        return Result.CONTINUE;
    }

    protected boolean addChange(CacheChange<D> cacheChange) {
        logger.entering("addChange");
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
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(RtpsMessage message) {
        try {
            process(message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        logger.severe(throwable);
        throw new UnsupportedOperationException();
    }

    @Override
    public void onComplete() {}

    protected void processInlineQos(Guid writer, Map<ParameterId, ?> params) {
        // empty
    }

    @Override
    public String toString() {
        return tracingToken.toString();
    }

    @Override
    public void close() {
        subscription.cancel();
        super.close();
        logger.fine("Closed");
    }
}
