package pinorobotics.rtpstalk.discovery.spdp;

import id.xfunction.logging.XLogger;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import pinorobotics.rtpstalk.io.RtpsMessageReader;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.walk.Result;
import pinorobotics.rtpstalk.messages.walk.RtpsMessageVisitor;
import pinorobotics.rtpstalk.messages.walk.RtpsMessageWalker;

public class SpdpBuiltinParticipantReader {

    private static final XLogger LOGGER = XLogger.getLogger(SpdpBuiltinParticipantReader.class);
    private ExecutorService executor = ForkJoinPool.commonPool();
    private Map<Guid, RtpsMessage> historyCache = new HashMap<>();
    private RtpsMessageReader reader = new RtpsMessageReader();
    private RtpsMessageWalker walker = new RtpsMessageWalker();
    private DatagramChannel dc;
    private int packetBufferSize;

    public SpdpBuiltinParticipantReader(DatagramChannel dc, int packetBufferSize) {
        this.packetBufferSize = packetBufferSize;
        this.dc = dc;
    }

    public void start() throws Exception {
        executor.execute(() -> {
            var thread = Thread.currentThread();
            LOGGER.fine("Running SPDPbuiltinParticipantReader on thread {0} with id {1}", thread.getName(),
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

    public Map<Guid, RtpsMessage> getParticipants() {
        return historyCache;
    }

    private void process(RtpsMessage message) {
        LOGGER.fine("Processing RTPS message {0}", message);
        walker.walk(message, new RtpsMessageVisitor() {
            @Override
            public Result onData(Data d) {
                if (d.serializedPayload.payload instanceof ParameterList pl) {
                    pl.findParameter(ParameterId.PID_PARTICIPANT_GUID).ifPresent(value -> {
                        var guid = (Guid) value;
                        if (historyCache.containsKey(guid)) {
                            LOGGER.fine("Message with GUID {0} already exist", guid);
                            return;
                        }
                        LOGGER.fine("Message with GUID {0} is new, adding it into the cache", guid);
                        historyCache.put(guid, message);
                    });
                }
                return Result.CONTINUE;
            }
        });
    }

}
