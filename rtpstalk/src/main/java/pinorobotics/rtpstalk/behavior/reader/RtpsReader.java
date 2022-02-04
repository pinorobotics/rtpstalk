package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.XAsserts;
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

/**
 * Each reader can be subscribed to only one data channel. And to one data
 * channel can be subscribed multiple of readers (many readers to one data
 * channel). Data channel it is where multiple remote writers (Participants)
 * send RTPS messages. When reader is subscribed to the data channel it is going
 * to receive all RTPS messages from it. Since one RTPS message can contain
 * submessages which belong to different readers it is reader responsibility to
 * filter them out.
 */
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

    protected void process(RtpsMessage message) {
        walker.walk(message, filterVisitor);
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
        XAsserts.assertNull(this.subscription, "Already subscribed");
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
