package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.ReliabilityKind;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.walk.Result;
import pinorobotics.rtpstalk.messages.walk.RtpsSubmessageVisitor;
import pinorobotics.rtpstalk.messages.walk.RtpsSubmessagesWalker;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.structure.HistoryCache;
import pinorobotics.rtpstalk.structure.RtpsEntity;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;

/**
 * Stateless RTPS endpoint reader (best-effort reliability) which can be
 * subscribed to {@link RtpsMessageReceiver} to receive RTPS messages and
 * process them.
 * 
 * <p>
 * Each reader can be subscribed to only one {@link RtpsMessageReceiver}. And to
 * one {@link RtpsMessageReceiver} can be subscribed multiple different readers
 * (many endpoints to one receiver).
 * 
 * <pre>
 * {@code
 * 
 * USER subscribes to:
 * - {@link RtpsReader} subscribes to:
 *  - {@link RtpsMessageReceiver} receives messages from single data channel
 *   - remove writer endpoint1
 *   - remove writer endpoint2
 *   - ...
 * 
 * }
 * </pre>
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

    public RtpsReader(Guid guid) {
        this(guid, ReliabilityKind.BEST_EFFORT);
    }

    public RtpsReader(Guid readerGuid, ReliabilityKind reliabilityKind) {
        this.guid = readerGuid;
        this.reliabilityKind = reliabilityKind;
        filterVisitor = new FilterByEntityIdRtpsSubmessageVisitor(readerGuid.entityId, this);
        logger = InternalUtils.getInstance().getLogger(getClass(), readerGuid.entityId);
    }

    /**
     * Contains the history of CacheChange changes for this RTPS Reader.
     */
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
        addChange(
                new CacheChange<>(new Guid(guidPrefix, d.writerId), d.writerSN.value, (D) d.serializedPayload.payload));
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
        XAsserts.assertNotNull(subscription, "Already subscribed");
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
    public void onComplete() {
        logger.severe(new UnsupportedOperationException());
    }
}
