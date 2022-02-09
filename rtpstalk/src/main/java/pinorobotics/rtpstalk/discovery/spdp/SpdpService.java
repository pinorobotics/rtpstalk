package pinorobotics.rtpstalk.discovery.spdp;

import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;

public class SpdpService implements AutoCloseable {

    private static final XLogger LOGGER = XLogger.getLogger(SpdpService.class);
    private RtpsTalkConfiguration config;
    private RtpsMessageReceiver receiver;
    private SpdpBuiltinParticipantReader reader;
    private SpdpBuiltinParticipantWriter writer;
    private boolean isStarted;
    private DataChannelFactory channelFactory;
    private SpdpDiscoveredParticipantDataFactory spdpDiscoveredDataFactory;

    public SpdpService(RtpsTalkConfiguration config, DataChannelFactory channelFactory) {
        this(config, channelFactory, new SpdpDiscoveredParticipantDataFactory());
    }

    public SpdpService(RtpsTalkConfiguration config, DataChannelFactory channelFactory,
            SpdpDiscoveredParticipantDataFactory spdpDiscoveredDataFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
        this.spdpDiscoveredDataFactory = spdpDiscoveredDataFactory;
        receiver = new RtpsMessageReceiver("SpdpServiceReceiver");
        reader = new SpdpBuiltinParticipantReader(config.getGuidPrefix());
    }

    public void start() throws Exception {
        LOGGER.entering("start");
        XAsserts.assertTrue(!isStarted, "Already started");
        LOGGER.fine("Using following configuration: {0}", config);
        Locator defaultMulticastLocator = Locator.createDefaultMulticastLocator(config.getDomainId());
        var dataChannel = channelFactory.bind(defaultMulticastLocator);
        receiver.start(dataChannel);
        receiver.subscribe(reader);
        writer = new SpdpBuiltinParticipantWriter(config, dataChannel.getDatagramChannel(),
                defaultMulticastLocator.address());
        writer.setSpdpDiscoveredParticipantData(spdpDiscoveredDataFactory.createData(config));
        writer.start();
        isStarted = true;
    }

    public SpdpBuiltinParticipantReader getReader() {
        return reader;
    }

    @Override
    public void close() throws Exception {
        receiver.close();
        writer.close();
    }
}
