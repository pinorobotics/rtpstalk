package pinorobotics.rtpstalk.behavior.writer;

import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.ReliabilityKind;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.structure.HistoryCache;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageSender;

public class StatefullRtpsWriter<D extends Payload> extends RtpsWriter<D> implements AutoCloseable {

    private static final XLogger LOGGER = XLogger.getLogger(StatefullRtpsWriter.class);
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("SpdpBuiltinParticipantWriter"));

    /**
     * Used to maintain state on the remote Readers matched up with this Writer.
     */
    private Map<Guid, ReaderProxy> matchedReaders = new ConcurrentHashMap<>();

    private HistoryCache<D> historyCache = new HistoryCache<>();
    private DataChannelFactory channelFactory;

    public StatefullRtpsWriter(DataChannelFactory channelFactory, Guid writerGuid, EntityId readerEntiyId) {
        super(writerGuid, readerEntiyId, ReliabilityKind.RELIABLE, true);
        this.channelFactory = channelFactory;
    }

    /**
     * Contains the history of CacheChange changes for this RTPS Writer.
     */
    public HistoryCache<D> getWriterCache() {
        return historyCache;
    }

    @Override
    public void newChange(D data) {
        super.newChange(data);
        historyCache.addChange(new CacheChange<>(getGuid(), getLastChangeNumber(), data));
    }

    public void matchedReaderAdd(Guid remoteGuid, List<Locator> unicast) throws IOException {
        var writerName = getGuid().entityId.toString();
        if (matchedReaders.containsKey(remoteGuid)) {
            LOGGER.fine("Reader {0} is already registered with the writer {1}, not adding it", remoteGuid,
                    writerName);
            return;
        }
        var proxy = new ReaderProxy(remoteGuid, unicast);
        LOGGER.fine("Adding writer proxy for writer with guid {0}", proxy.getRemoteReaderGuid());
        matchedReaders.put(proxy.getRemoteReaderGuid(), proxy);
        var sender = new RtpsMessageSender(channelFactory.connect(unicast.get(0)), writerName);
        subscribe(sender);
    }

    public void matchedWriterRemove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        super.close();
        executor.shutdown();
    }
}
