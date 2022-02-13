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
import pinorobotics.rtpstalk.structure.RtpsEntity;

/**
 * Stateless RTPS writer (best-effort reliability).
 * 
 * <pre>
 * {@code
 * 
 * USER calls {@link RtpsWriter#newChange}:
 * - {@link RtpsWriter} publishes change to all its connected subscribers (in accordance with
 * RTPS these will be endpoint readers or reader locators):
 *  - {@link RtpsMessageSender} sends message to remote reader1
 *  - {@link RtpsMessageSender} sends message to remote reader2
 *  - ...
 * }
 * </pre>
 */
public class RtpsWriter extends SubmissionPublisher<RtpsMessage> implements RtpsEntity {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsWriter.class);

    private int lastChangeNumber;
    private Guid writerGuid;
    private EntityId readerEntiyId;
    private RtpsMessage lastMessage;

    public RtpsWriter(Guid writerGuid, EntityId readerEntiyId) {
        this(writerGuid, readerEntiyId, ReliabilityKind.BEST_EFFORT);
    }

    public RtpsWriter(Guid writerGuid, EntityId readerEntiyId, ReliabilityKind reliabilityKind) {
        this.writerGuid = writerGuid;
        this.readerEntiyId = readerEntiyId;
    }

    /**
     * Internal counter used to assign increasing sequence number to each change
     * made by the Writer.
     */
    public int getLastChangeNumber() {
        return lastChangeNumber;
    }

    public void repeatLastChange() {
        XAsserts.assertNotNull(lastMessage);
        LOGGER.entering("repeatLastChange");
        submit(lastMessage);
        LOGGER.exiting("repeatLastChange");
    }

    /**
     * This operation creates a new CacheChange to be appended to the RTPS Writer`s
     * HistoryCache. The sequence number of the CacheChange is automatically set to
     * be the sequenceNumber of the previous change plus one.
     */
    public void newChange(Payload data) {
        LOGGER.entering("newChange");
        lastChangeNumber++;
        var dataSubmessage = new Data(0b100 | RtpsTalkConfiguration.ENDIANESS_BIT, 0,
                readerEntiyId,
                writerGuid.entityId,
                new SequenceNumber(lastChangeNumber),
                new SerializedPayload(new SerializedPayloadHeader(
                        RepresentationIdentifier.Predefined.PL_CDR_LE.getValue()),
                        data));
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
}
