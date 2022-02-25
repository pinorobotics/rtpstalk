package pinorobotics.rtpstalk.behavior.writer;

import id.xfunction.XAsserts;
import id.xfunction.concurrent.NamedThreadFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.ReliabilityKind;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import java.util.concurrent.TimeUnit;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.OperatingEntities;
import pinorobotics.rtpstalk.messages.Duration;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.structure.HistoryCache;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageSender;

public class StatefullRtpsWriter<D extends Payload> extends RtpsWriter<D> implements Runnable, AutoCloseable {

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("SpdpBuiltinParticipantWriter"));

    /**
     * Used to maintain state on the remote Readers matched up with this Writer.
     */
    private Map<Guid, ReaderProxy> matchedReaders = new HashMap<>();

    /**
     * Protocol tuning parameter that allows the RTPS Writer to repeatedly announce
     * the availability of data by sending a Heartbeat Message.
     */
    private Duration heartbeatPeriod;
    private HistoryCache<D> historyCache = new HistoryCache<>();
    private int heartbeatCount;
    private DataChannelFactory channelFactory;
    private String writerName;
    private final Header header;

    public StatefullRtpsWriter(DataChannelFactory channelFactory, Guid writerGuid, EntityId readerEntiyId,
            Duration heartbeatPeriod) {
        super(writerGuid, readerEntiyId, ReliabilityKind.RELIABLE, true);
        this.channelFactory = channelFactory;
        this.heartbeatPeriod = heartbeatPeriod;
        writerName = getGuid().entityId.toString();
        OperatingEntities.getInstance().add(writerGuid.entityId, this);
        header = new Header(
                ProtocolId.Predefined.RTPS.getValue(),
                ProtocolVersion.Predefined.Version_2_3.getValue(),
                VendorId.Predefined.RTPSTALK.getValue(),
                getGuid().guidPrefix);
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

    public synchronized void matchedReaderAdd(Guid remoteGuid, List<Locator> unicast) throws IOException {
        if (matchedReaders.containsKey(remoteGuid)) {
            logger.fine("Reader {0} is already registered with the writer {1}, not adding it", remoteGuid,
                    writerName);
            return;
        }
        var sender = new RtpsMessageSender(channelFactory.connect(unicast.get(0)), writerName, remoteGuid.guidPrefix);
        var proxy = new ReaderProxy(remoteGuid, unicast, sender);
        logger.fine("Adding reader proxy for reader with guid {0}", proxy.getRemoteReaderGuid());
        var numOfReaders = matchedReaders.size();
        matchedReaders.put(proxy.getRemoteReaderGuid(), proxy);
        subscribe(proxy.getSender());
        if (numOfReaders == 0) {
            executor.scheduleWithFixedDelay(this, 0, heartbeatPeriod.seconds, TimeUnit.SECONDS);
        }
    }

    public void matchedReaderRemove() {
        throw new UnsupportedOperationException();
    }

    /**
     * This operation finds the {@link ReaderProxy} with given {@link Guid}
     */
    public Optional<ReaderProxy> matchedReaderLookup(Guid guid) {
        return Optional.ofNullable(matchedReaders.get(guid));
    }

    @Override
    public void run() {
        try {
            if (executor.isShutdown())
                return;
            sendRequested();
            sendHeartbeat();
        } catch (Exception e) {
            logger.severe("Writer " + writerName + " heartbeat error", e);
        }
    }

    @Override
    public void close() {
        super.close();
        executor.shutdown();
        OperatingEntities.getInstance().remove(getGuid().entityId);
    }

    private void sendHeartbeat() {
        var seqNumMin = historyCache.getSeqNumMin();
        if (seqNumMin <= 0) {
            logger.fine("Skipping heartbeat since history cache is empty");
            return;
        }
        var seqNumMax = historyCache.getSeqNumMax();
        XAsserts.assertLess(0, seqNumMax, "Negative sequence number");
        var heartbeat = new Heartbeat(getReaderEntiyId(), getGuid().entityId, new SequenceNumber(seqNumMin),
                new SequenceNumber(seqNumMax), new Count(heartbeatCount++));
        var submessages = new Submessage[] { heartbeat };
        submit(new RtpsMessage(header, submessages));
        logger.fine("Heartbeat submitted");
    }

    private void sendRequested() {
        matchedReaders.values().stream()
                .forEach(this::sendRequested);
    }

    private void sendRequested(ReaderProxy readerProxy) {
        var requestedChanges = readerProxy.requestedChanges();
        if (requestedChanges.isEmpty())
            return;
        var submessages = historyCache.findAll(requestedChanges)
                .map(change -> new Data(0b100 | RtpsTalkConfiguration.ENDIANESS_BIT, 0,
                        readerProxy.getRemoteReaderGuid().entityId,
                        getGuid().entityId,
                        new SequenceNumber(change.getSequenceNumber()),
                        new SerializedPayload(PAYLOAD_HEADER, change.getDataValue())))
                .toArray(Submessage[]::new);
        if (submessages.length == 0)
            return;
        submit(new RtpsMessage(header, submessages));
        logger.fine("Submitted {0} requested changes", requestedChanges.size());
    }
}
