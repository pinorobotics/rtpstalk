package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.logging.XLogger;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import pinorobotics.rtpstalk.io.RtpsMessageReader;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.walk.Result;
import pinorobotics.rtpstalk.messages.walk.RtpsMessageVisitor;
import pinorobotics.rtpstalk.messages.walk.RtpsMessageWalker;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.structure.HistoryCache;

public class StatelessRtpsReader {

    private static final XLogger LOGGER = XLogger.getLogger(StatelessRtpsReader.class);

    /**
     * Contains the history of CacheChange changes for this RTPS Reader.
     */
    private HistoryCache cache = new HistoryCache();

    private ExecutorService executor = ForkJoinPool.commonPool();
    private RtpsMessageReader reader = new RtpsMessageReader();
    private RtpsMessageWalker walker = new RtpsMessageWalker();
    private DatagramChannel dc;
    private int packetBufferSize;

    public StatelessRtpsReader(DatagramChannel dc, int packetBufferSize) {
        this.packetBufferSize = packetBufferSize;
        this.dc = dc;
    }

    public void start() throws Exception {
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
        walker.walk(message, new RtpsMessageVisitor() {
            @Override
            public Result onData(RtpsMessage message, Data d) {
                cache.addChange(new CacheChange(new Guid(message.header.guidPrefix, d.writerId), d.writerSN, d));
                return Result.CONTINUE;
            }
        });
    }

    protected HistoryCache getCache() {
        return cache;
    }
}
