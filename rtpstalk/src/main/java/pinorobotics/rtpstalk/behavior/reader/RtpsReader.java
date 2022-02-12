package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.ReliabilityKind;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
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
 *  - {@link RtpsMessageReceiver} receives messages from:
 *   - remote RTPS writer1
 *   - remote RTPS writer2
 *   - ...
 * 
 * }
 * </pre>
 */
public class RtpsReader extends SubmissionPublisher<CacheChange>
        implements RtpsEntity, Subscriber<RtpsMessage>, RtpsSubmessageVisitor {

    private final XLogger LOGGER = XLogger.getLogger(getClass());

    private HistoryCache cache = new HistoryCache();
    private RtpsSubmessagesWalker walker = new RtpsSubmessagesWalker();
    private Guid guid;
    private RtpsSubmessageVisitor filterVisitor;
    private ReliabilityKind reliabilityKind;
    private Subscription subscription;

    public RtpsReader(Guid guid) {
        this(guid, ReliabilityKind.BEST_EFFORT);
    }

    public RtpsReader(Guid guid, ReliabilityKind reliabilityKind) {
        this.guid = guid;
        this.reliabilityKind = reliabilityKind;
        filterVisitor = new FilterByEntityIdRtpsSubmessageVisitor(guid.entityId, this);
    }

    /**
     * Contains the history of CacheChange changes for this RTPS Reader.
     */
    public HistoryCache getReaderCache() {
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
        LOGGER.fine("Received data {0}", d);
        addChange(new CacheChange(new Guid(guidPrefix, d.writerId), d.writerSN, d.serializedPayload.payload));
        return Result.CONTINUE;
    }

    protected void addChange(CacheChange cacheChange) {
        LOGGER.entering("addChange");
        cache.addChange(cacheChange);
        submit(cacheChange);
        LOGGER.exiting("addChange");
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
        LOGGER.severe(throwable);
        throw new UnsupportedOperationException();
    }

    @Override
    public void onComplete() {
        LOGGER.severe(new UnsupportedOperationException());
    }
}
