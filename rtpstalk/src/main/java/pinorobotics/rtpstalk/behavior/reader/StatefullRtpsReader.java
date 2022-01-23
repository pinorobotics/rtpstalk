package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.logging.XLogger;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.walk.Result;

public class StatefullRtpsReader extends RtpsReader {

    private static final XLogger LOGGER = XLogger.getLogger(StatefullRtpsReader.class);

    /**
     * Used to maintain state on the remote Writers matched up with the Reader
     */
    private Map<Guid, WriterProxy> matchedWriters = new HashMap<>();

    public StatefullRtpsReader(DatagramChannel dc, int packetBufferSize) {
        super(dc, packetBufferSize);
    }

    public void matchedWriterAdd(WriterProxy proxy) {
        LOGGER.fine("Adding writer proxy for writer with guid {0}", proxy.remoteWriterGuid());
        matchedWriters.put(proxy.remoteWriterGuid(), proxy);
    }

    public void matchedWriterRemove() {

    }

    public Optional<WriterProxy> matchedWriterLookup(Guid guid) {
        return Optional.ofNullable(matchedWriters.get(guid));
    }

    @Override
    public Result onData(RtpsMessage message, Data d) {
        return super.onData(message, d);
    }

    @Override
    public Result onHeartbeat(RtpsMessage message, Heartbeat heartbeat) {
        System.out.println(heartbeat);
        return super.onHeartbeat(message, heartbeat);
    }
}
