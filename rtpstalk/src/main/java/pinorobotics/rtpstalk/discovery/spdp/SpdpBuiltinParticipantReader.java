package pinorobotics.rtpstalk.discovery.spdp;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.io.RtpsMessageReader;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.SubmessageKind.Predefined;
import pinorobotics.rtpstalk.messages.submessages.elements.Parameter;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;

public class SpdpBuiltinParticipantReader {

    private static final XLogger LOGGER = XLogger.getLogger(SpdpBuiltinParticipantReader.class);
    private ExecutorService executor = ForkJoinPool.commonPool();
    private Map<Guid, RtpsMessage> historyCache = new HashMap<>();
    private RtpsMessageReader reader = new RtpsMessageReader();
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
        findParameterValues(message, ParameterId.PID_PARTICIPANT_GUID)
                .findFirst()
                .ifPresent(value -> {
                    var guid = (Guid) value;
                    if (historyCache.containsKey(guid)) {
                        LOGGER.fine("Message with GUID {0} already exist", guid);
                        return;
                    }
                    LOGGER.fine("Message with GUID {0} is new, adding it into the cache", guid);
                    historyCache.put(guid, message);
                });
    }

    public static Stream<Data> findDataElements(RtpsMessage message) {
        return Arrays.stream(message.getSubmessages())
                .filter(Submessage.filterBySubmessageKind(Predefined.DATA.getValue()))
                .map(e -> (Data) e);
    }

    public static Stream<Object> findParameterValues(RtpsMessage message, ParameterId paramId) {
        return findDataElements(message)
                .map(SpdpBuiltinParticipantReader::findParameterList)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ParameterList::getParameters)
                .flatMap(List::stream)
                .filter(p -> p.parameterId() == paramId)
                .map(Parameter::value);
    }

    public static Optional<ParameterList> findParameterList(Data data) {
        return getParameterList(data.getSerializedPayload());
    }

    public static Optional<ParameterList> getParameterList(SerializedPayload payload) {
        if (payload.payload instanceof ParameterList pl) {
            return Optional.of(pl);
        }
        return Optional.empty();
    }

}
