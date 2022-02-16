package pinorobotics.rtpstalk.discovery.sedp;

import id.xfunction.XAsserts;
import id.xfunction.concurrent.flow.XSubscriber;
import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Flow.Publisher;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.liveliness.BuiltinParticipantMessageReader;
import pinorobotics.rtpstalk.behavior.reader.RtpsReader;
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.behavior.reader.WriterProxy;
import pinorobotics.rtpstalk.behavior.writer.RtpsWriter;
import pinorobotics.rtpstalk.messages.BuiltinEndpointQos.EndpointQos;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet.Endpoint;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.transport.DataChannelFactory;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;

/**
 * Using the SPDPbuiltinParticipantReader, a local Participant
 * ‘local_participant’ discovers the existence of another Participant described
 * by the DiscoveredParticipantData participant_data. The discovered Participant
 * uses the SEDP (8.5.5.1 Discovery of a new remote Participant)
 */
public class SedpService extends XSubscriber<CacheChange<ParameterList>> {

    private static final XLogger LOGGER = XLogger.getLogger(SedpService.class);
    private RtpsTalkConfiguration config;
    private SedpBuiltinSubscriptionsReader subscriptionsReader;
    private SedpBuiltinSubscriptionsWriter subscriptionsWriter;
    private SedpBuiltinPublicationsReader publicationsReader;
    private RtpsMessageReceiver receiver;
    private boolean isStarted;
    private DataChannelFactory channelFactory;

    public SedpService(RtpsTalkConfiguration config, DataChannelFactory channelFactory) {
        this.config = config;
        this.channelFactory = channelFactory;
        receiver = new RtpsMessageReceiver("SedpServiceReceiver");
        subscriptionsWriter = new SedpBuiltinSubscriptionsWriter(config);
    }

    public void start(Publisher<CacheChange<ParameterList>> participantsPublisher) throws IOException {
        LOGGER.entering("start");
        XAsserts.assertTrue(!isStarted, "Already started");
        LOGGER.fine("Using following configuration: {0}", config);

        subscriptionsReader = new SedpBuiltinSubscriptionsReader(config);
        receiver.subscribe(subscriptionsReader);
        publicationsReader = new SedpBuiltinPublicationsReader(config);
        receiver.subscribe(publicationsReader);
        if (config.getBuiltinEndpointQos() == EndpointQos.NONE)
            receiver.subscribe(new BuiltinParticipantMessageReader(config));
        participantsPublisher.subscribe(this);
        receiver.start(channelFactory.bind(config.getMetatrafficUnicastLocator()));
        isStarted = true;
    }

    @Override
    public void onNext(CacheChange<ParameterList> change) {
        LOGGER.entering("onNext");
        configureEndpoints(change.getWriterGuid().guidPrefix, change.getDataValue());
        subscription.request(1);
        LOGGER.exiting("onNext");
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.severe(throwable);
    }

    @Override
    public void onComplete() {

    }

    public RtpsReader<ParameterList> getPublicationsReader() {
        return publicationsReader;
    }

    public StatefullRtpsReader<ParameterList> getSubscriptionsReader() {
        return subscriptionsReader;
    }

    private void configureEndpoints(GuidPrefix guidPrefix, ParameterList participantData) {
        LOGGER.fine("Configuring builtin endpoints for Participant {0}", guidPrefix);
        var params = participantData.getParameters();
        var value = params.get(ParameterId.PID_BUILTIN_ENDPOINT_SET);
        if (value instanceof BuiltinEndpointSet availableEndpoints) {
            if (params.get(ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR) instanceof Locator locator) {
                var unicast = List.of(locator);
                configure(availableEndpoints, guidPrefix, subscriptionsReader,
                        Endpoint.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_ANNOUNCER, unicast);
                configure(availableEndpoints, guidPrefix, publicationsReader,
                        Endpoint.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_ANNOUNCER, unicast);
            } else {
                LOGGER.fine("Participant has no locator defined, ignoring...");
            }
        } else {
            LOGGER.fine("Participant has no supported builtin endpoints, ignoring...");
        }
    }

    private void configure(BuiltinEndpointSet availableEndpoints, GuidPrefix guidPrefix,
            StatefullRtpsReader<ParameterList> reader,
            Endpoint endpoint, List<Locator> unicast) {
        if (!availableEndpoints.hasEndpoint(endpoint)) {
            LOGGER.fine(
                    "Participant does not support {0} endpoint, ignoring...", endpoint);
            return;
        }
        reader.matchedWriterAdd(new WriterProxy(reader.getGuid(),
                new Guid(guidPrefix, endpoint.getEntityId().getValue()),
                unicast));
    }

    public RtpsWriter<ParameterList> getSubscriptionsWriter() {
        return subscriptionsWriter;
    }
}
