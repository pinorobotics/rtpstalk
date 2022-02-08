package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.logging.XLogger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.ReliabilityKind;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.walk.Result;
import pinorobotics.rtpstalk.structure.CacheChange;

/**
 * Reliable Statefull RTPS reader.
 * 
 * <p>
 * This should be thread safe since it is possible that one thread will be
 * calling {@link #matchedWriterAdd(WriterProxy)} when another doing
 * {@link #process(RtpsMessage)} and that happening at the same time.
 */
public class StatefullRtpsReader extends RtpsReader {

    private final XLogger LOGGER = XLogger.getLogger(getClass());

    /**
     * Used to maintain state on the remote Writers matched up with the Reader.
     */
    private Map<Guid, WriterInfo> matchedWriters = new ConcurrentHashMap<>();

    private RtpsTalkConfiguration config;

    public StatefullRtpsReader(RtpsTalkConfiguration config, EntityId entityId) {
        super(new Guid(config.getGuidPrefix(), entityId), ReliabilityKind.RELIABLE);
        this.config = config;
    }

    public void matchedWriterAdd(WriterProxy proxy) {
        LOGGER.fine("Adding writer proxy for writer with guid {0}", proxy.getRemoteWriterGuid());
        matchedWriters.put(proxy.getRemoteWriterGuid(),
                new WriterInfo(proxy, new WriterHeartbeatProcessor(config, proxy)));
    }

    public void matchedWriterRemove() {

    }

    public Optional<WriterProxy> matchedWriterLookup(Guid guid) {
        return Optional.ofNullable(matchedWriters.get(guid))
                .map(WriterInfo::proxy);
    }

    public List<WriterProxy> matchedWriters() {
        return matchedWriters.values().stream()
                .map(WriterInfo::proxy)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Result onHeartbeat(GuidPrefix guidPrefix, Heartbeat heartbeat) {
        // However, if the FinalFlag is not set, then the Reader must send an AckNack
        // message (8.3.7.5.5)
        if (!heartbeat.isFinal()) {
            var writerGuid = new Guid(guidPrefix, heartbeat.writerId);
            var writerInfo = matchedWriters.get(writerGuid);
            if (writerInfo != null) {
                LOGGER.fine("Received heartbeat from writer {0}", writerGuid);
                writerInfo.heartbeatProcessor().addHeartbeat(heartbeat);
            } else {
                LOGGER.fine("Received heartbeat from unknown writer {0}, ignoring...", writerGuid);
            }
        }
        return super.onHeartbeat(guidPrefix, heartbeat);
    }

    @Override
    protected void addChange(CacheChange cacheChange) {
        super.addChange(cacheChange);
        var writerInfo = matchedWriters.get(cacheChange.getWriterGuid());
        if (writerInfo == null) {
            LOGGER.fine("No matched writer with guid {0} found for a new change, ignoring...",
                    cacheChange.getWriterGuid());
            return;
        }
        writerInfo.proxy().addChange(cacheChange);
        return;
    }

    @Override
    protected void process(RtpsMessage message) {
        super.process(message);
        matchedWriters.values().stream()
                .map(WriterInfo::heartbeatProcessor)
                .forEach(WriterHeartbeatProcessor::ack);
    }
}
