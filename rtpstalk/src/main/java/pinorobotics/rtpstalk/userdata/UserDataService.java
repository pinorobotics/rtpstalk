package pinorobotics.rtpstalk.userdata;

import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;

public class UserDataService {

    private static final XLogger LOGGER = XLogger.getLogger(UserDataService.class);
    private RtpsTalkConfiguration config;
    private RtpsMessageReceiver receiver;
    private DataChannelFactory channelFactory;
    private Map<EntityId, DataReader> readers = new HashMap<>();
    private boolean isStarted;

    public UserDataService(RtpsTalkConfiguration config, DataChannelFactory channelFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
        receiver = new RtpsMessageReceiver("UserDataServiceReceiver");
    }

    public void subscribe(EntityId entityId, Subscriber<CacheChange<RawData>> subscriber) {
        var reader = readers.computeIfAbsent(entityId, eid -> new DataReader(config, eid));
        reader.subscribe(subscriber);
        receiver.subscribe(reader);
    }

    public void start() throws IOException {
        LOGGER.entering("start");
        XAsserts.assertTrue(!isStarted, "Already started");
        LOGGER.fine("Using following configuration: {0}", config);
        receiver.start(channelFactory.bind(config.getDefaultUnicastLocator()));
        isStarted = true;
    }
}
