package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.logging.XLogger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.walk.Result;
import pinorobotics.rtpstalk.structure.CacheChange;

public class StatefullRtpsReader extends RtpsReader {

    private final XLogger LOGGER = XLogger.getLogger(getClass());

    /**
     * Used to maintain state on the remote Writers matched up with the Reader
     */
    private Map<Guid, WriterProxy> matchedWriters = new HashMap<>();

    public StatefullRtpsReader(Guid guid) {
        super(guid);
    }

    public void matchedWriterAdd(WriterProxy proxy) {
        LOGGER.fine("Adding writer proxy for writer with guid {0}", proxy.getRemoteWriterGuid());
        matchedWriters.put(proxy.getRemoteWriterGuid(), proxy);
    }

    public void matchedWriterRemove() {

    }

    public Optional<WriterProxy> matchedWriterLookup(Guid guid) {
        return Optional.ofNullable(matchedWriters.get(guid));
    }

    @Override
    public Result onHeartbeat(GuidPrefix guidPrefix, Heartbeat heartbeat) {
        // However, if the FinalFlag is not set, then the Reader must send an AckNack
        // message (8.3.7.5.5)
        if (!heartbeat.isFinal()) {
            var writerGuid = new Guid(guidPrefix, heartbeat.writerId);
            var writerProxy = matchedWriters.get(writerGuid);
            if (writerProxy != null) {
                writerProxy.onHeartbeat(heartbeat);
            } else {
                LOGGER.fine("Received heartbeat from unknown writer {0}, ignoring...", writerGuid);
            }
        }
        return super.onHeartbeat(guidPrefix, heartbeat);
    }

    @Override
    protected void addChange(CacheChange cacheChange) {
        super.addChange(cacheChange);
        var matchedWriter = matchedWriters.get(cacheChange.getWriterGuid());
        if (matchedWriter == null) {
            LOGGER.fine("No matched writer with guid {0} found for a new change, ignoring...",
                    cacheChange.getWriterGuid());
            return;
        }
        matchedWriter.addChange(cacheChange);
        return;
    }
}
