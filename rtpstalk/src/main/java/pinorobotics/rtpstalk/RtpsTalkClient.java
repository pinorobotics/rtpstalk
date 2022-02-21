package pinorobotics.rtpstalk;

import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.discovery.sedp.SedpService;
import pinorobotics.rtpstalk.discovery.spdp.SpdpService;
import pinorobotics.rtpstalk.exceptions.RtpsTalkException;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.KeyHash;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.userdata.UserDataService;

public class RtpsTalkClient {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsTalkClient.class);
    private RtpsTalkConfiguration config;
    private DataChannelFactory channelFactory;
    private SpdpService spdp;
    private SedpService sedp;
    private UserDataService userService;
    private boolean isStarted;

    public RtpsTalkClient() {
        this(RtpsTalkConfiguration.DEFAULT);
    }

    public RtpsTalkClient(RtpsTalkConfiguration config) {
        this.config = config;
        channelFactory = new DataChannelFactory(config);
        spdp = new SpdpService(config, channelFactory);
        sedp = new SedpService(config, channelFactory);
        userService = new UserDataService(config, channelFactory);
    }

    public void subscribe(String topic, String type, EntityId entityId, Subscriber<CacheChange<RawData>> subscriber) {
        if (!isStarted) {
            start();
        }
        sedp.getSubscriptionsWriter().newChange(createSubscriptionData(topic, type, entityId));
        userService.subscribe(entityId, subscriber);
    }

    private void start() {
        LOGGER.entering("start");
        XAsserts.assertTrue(!isStarted, "Already started");
        LOGGER.fine("Using following configuration: {0}", config);
        try {
            sedp.start(spdp.getReader());
            spdp.start();
            userService.start();
        } catch (Exception e) {
            throw new RtpsTalkException(e);
        }
        isStarted = true;
        LOGGER.exiting("start");
    }

    private ParameterList createSubscriptionData(String topicName, String typeName, EntityId entityId) {
        var params = List.<Entry<ParameterId, Object>>of(
                Map.entry(ParameterId.PID_UNICAST_LOCATOR, config.getDefaultUnicastLocator()),
                Map.entry(ParameterId.PID_PARTICIPANT_GUID, new Guid(
                        config.getGuidPrefix(), EntityId.Predefined.ENTITYID_PARTICIPANT.getValue())),
                Map.entry(ParameterId.PID_TOPIC_NAME, topicName),
                Map.entry(ParameterId.PID_TYPE_NAME, typeName),
                Map.entry(ParameterId.PID_KEY_HASH, new KeyHash(
                        0x01, 0x0f, 0xeb, 0x7d, 0x5f, 0xfa, 0x9f, 0xe4, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x12,
                        0x04)),
                Map.entry(ParameterId.PID_ENDPOINT_GUID,
                        new Guid(config.getGuidPrefix(), entityId)),
                Map.entry(ParameterId.PID_PROTOCOL_VERSION, ProtocolVersion.Predefined.Version_2_3.getValue()),
                Map.entry(ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue()));
        return new ParameterList(params);
    }
}
