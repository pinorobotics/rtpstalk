package pinorobotics.rtpstalk.behavior.writer;

import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import java.util.concurrent.SubmissionPublisher;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.ReliabilityKind;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.RepresentationIdentifier;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.structure.HistoryCache;
import pinorobotics.rtpstalk.structure.RtpsEntity;

/**
 * <p>
 * This writer does not cache changes in {@link HistoryCache} and sends them to
 * readers directly (although it is used in {@link StatefullRtpsWriter}).
 * 
 * <p>
 * Data flow:
 * 
 * <pre>
 * {@code
 * 
 * USER calls {@link RtpsWriter#newChange}:
 * - {@link RtpsWriter} publishes change to all its connected subscribers (endpoint readers):
 *  - {@link RtpsMessageSender} sends message to remote reader1
 *  - {@link RtpsMessageSender} sends message to remote reader2
 *  - ...
 * }
 * </pre>
 */
public class RtpsWriter<D extends Payload> extends SubmissionPublisher<RtpsMessage> implements RtpsEntity {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsWriter.class);

    protected static final SerializedPayloadHeader PAYLOAD_HEADER = new SerializedPayloadHeader(
            RepresentationIdentifier.Predefined.PL_CDR_LE.getValue());

    private long lastChangeNumber;
    private Guid writerGuid;
    private EntityId readerEntiyId;
    private RtpsMessage lastMessage;

    public RtpsWriter(Guid writerGuid, EntityId readerEntiyId) {
        this(writerGuid, readerEntiyId, ReliabilityKind.BEST_EFFORT, true);
    }

    /**
     * @param pushMode Note that for a {@link ReliabilityKind.BEST_EFFORT} Writer,
     *                 pushMode is true, as there are no acknowledgments. Therefore,
     *                 the Writer always pushes out data as it becomes available
     *                 (8.4.9.1.1)
     */
    public RtpsWriter(Guid writerGuid, EntityId readerEntiyId,
            ReliabilityKind reliabilityKind, boolean pushMode) {
        this.writerGuid = writerGuid;
        this.readerEntiyId = readerEntiyId;
    }

    /**
     * Internal counter used to assign increasing sequence number to each change
     * made by the Writer.
     */
    public long getLastChangeNumber() {
        return lastChangeNumber;
    }

    public void repeatLastChange() {
        XAsserts.assertNotNull(lastMessage);
        LOGGER.entering("repeatLastChange");
        submit(lastMessage);
        LOGGER.exiting("repeatLastChange");
    }

    public void newChange(D data) {
        LOGGER.entering("newChange");
        lastChangeNumber++;
        var dataSubmessage = new Data(0b100 | RtpsTalkConfiguration.ENDIANESS_BIT, 0,
                readerEntiyId,
                writerGuid.entityId,
                new SequenceNumber(lastChangeNumber),
                new SerializedPayload(PAYLOAD_HEADER, data));
        var submessages = new Submessage[] { InfoTimestamp.now(), dataSubmessage };
        var header = new Header(
                ProtocolId.Predefined.RTPS.getValue(),
                ProtocolVersion.Predefined.Version_2_3.getValue(),
                VendorId.Predefined.RTPSTALK.getValue(),
                writerGuid.guidPrefix);
        lastMessage = new RtpsMessage(header, submessages);
        submit(lastMessage);
        LOGGER.exiting("newChange");
    }

    @Override
    public Guid getGuid() {
        return writerGuid;
    }

    public EntityId getReaderEntiyId() {
        return readerEntiyId;
    }
}
