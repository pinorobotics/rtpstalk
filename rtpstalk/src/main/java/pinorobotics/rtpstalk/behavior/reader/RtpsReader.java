package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.logging.XLogger;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.walk.Result;
import pinorobotics.rtpstalk.messages.walk.RtpsSubmessageVisitor;
import pinorobotics.rtpstalk.messages.walk.RtpsSubmessagesWalker;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.structure.HistoryCache;
import pinorobotics.rtpstalk.structure.RtpsEntity;

public class RtpsReader implements Subscriber<RtpsMessage>, RtpsEntity, RtpsSubmessageVisitor {

    private final XLogger LOGGER = XLogger.getLogger(getClass());

    /**
     * Contains the history of CacheChange changes for this RTPS Reader.
     */
    private HistoryCache cache = new HistoryCache();

    private RtpsSubmessagesWalker walker = new RtpsSubmessagesWalker();
    private Guid guid;

    private Subscription subscription;

    private RtpsSubmessageVisitor filterVisitor;

    public RtpsReader(Guid guid) {
        this.guid = guid;
        filterVisitor = new FilterByEntityIdRtpsSubmessageVisitor(guid.entityId, this);
    }

    @Override
    public Result onData(GuidPrefix guidPrefix, Data d) {
        LOGGER.fine("Received data {0}", d);
        addChange(new CacheChange(new Guid(guidPrefix, d.writerId), d.writerSN, d));
        return Result.CONTINUE;
    }

    protected void addChange(CacheChange cacheChange) {
        cache.addChange(cacheChange);
    }

    public HistoryCache getCache() {
        return cache;
    }

    @Override
    public Guid getGuid() {
        return guid;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(RtpsMessage message) {
        if (!message.header.guidPrefix.equals(guid.guidPrefix)) {
            walker.walk(message, filterVisitor);
        } else {
            LOGGER.fine("Received its own message, ignoring...");
        }
        subscription.request(1);
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
