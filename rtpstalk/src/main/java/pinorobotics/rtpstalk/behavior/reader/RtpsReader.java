package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.logging.XLogger;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
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
import pinorobotics.rtpstalk.transport.io.RtpsMessageReader;

public class RtpsReader implements RtpsEntity, RtpsSubmessageVisitor {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsReader.class);

    /**
     * Contains the history of CacheChange changes for this RTPS Reader.
     */
    private HistoryCache cache = new HistoryCache();

    private ExecutorService executor = ForkJoinPool.commonPool();
    private RtpsMessageReader reader = new RtpsMessageReader();
    private RtpsSubmessagesWalker walker = new RtpsSubmessagesWalker();
    private DatagramChannel dc;
    protected int packetBufferSize;
    private Guid guid;

    public RtpsReader(Guid guid, DatagramChannel dc, int packetBufferSize) {
        this.guid = guid;
        this.packetBufferSize = packetBufferSize;
        this.dc = dc;
    }

    public void start() {
        executor.execute(() -> {
            var thread = Thread.currentThread();
            LOGGER.fine("Running {0} on thread {1} with id {2}", getClass().getSimpleName(), thread.getName(),
                    thread.getId());
            while (!executor.isShutdown()) {
                try {
                    var buf = ByteBuffer.allocate(packetBufferSize);
                    dc.receive(buf);
                    var len = buf.position();
                    buf.rewind();
                    buf.limit(len);
                    reader.readRtpsMessage(buf).ifPresent(this::process);
                } catch (Exception e) {
                    LOGGER.severe(e);
                }
            }
            LOGGER.fine("Shutdown received, stopping...");
        });
    }

    private void process(RtpsMessage message) {
        LOGGER.fine("Incoming RTPS message {0}", message);
        if (message.header.guidPrefix.equals(guid.guidPrefix)) {
            LOGGER.fine("Received its own message, ignoring...");
            return;
        }
        walker.walk(message, this);
    }

    @Override
    public Result onData(GuidPrefix guidPrefix, Data d) {
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
}
