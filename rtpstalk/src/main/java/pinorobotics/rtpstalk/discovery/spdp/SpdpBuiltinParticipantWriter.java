package pinorobotics.rtpstalk.discovery.spdp;

import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.XLogger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import pinorobotics.rtpstalk.behavior.writer.RtpsWriter;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;

public class SpdpBuiltinParticipantWriter extends RtpsWriter<ParameterList> implements Runnable, AutoCloseable {

    private static final XLogger LOGGER = XLogger.getLogger(SpdpBuiltinParticipantWriter.class);
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("SpdpBuiltinParticipantWriter"));
    private ParameterList data;

    public SpdpBuiltinParticipantWriter(GuidPrefix guidPrefix) {
        super(new Guid(guidPrefix, EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_ANNOUNCER.getValue()),
                EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue());
    }

    public void start() {
        executor.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
    }

    public void setSpdpDiscoveredParticipantData(ParameterList data) {
        LOGGER.fine("Setting SpdpDiscoveredParticipantData {0}", data);
        this.data = data;
    }

    @Override
    public void run() {
        if (executor.isShutdown())
            return;
        if (data == null) {
            LOGGER.fine("No SpdpDiscoveredParticipantData to send, skipping");
            return;
        }
        switch ((int) getLastChangeNumber()) {
        case 0:
            newChange(data);
            break;
        case 1:
            repeatLastChange();
            break;
        default:
            throw new RuntimeException("Unexpected last change value " + getLastChangeNumber());
        }
        LOGGER.fine("Sent SpdpDiscoveredParticipantData");
    }

    @Override
    public void close() {
        super.close();
        executor.shutdown();
    }

}
