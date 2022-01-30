package pinorobotics.rtpstalk.discovery.sedp;

import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.reader.StatefullRtpsReader;
import pinorobotics.rtpstalk.behavior.reader.WriterProxy;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet.Endpoint;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.LocatorKind;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.structure.CacheChange;
import pinorobotics.rtpstalk.transport.RtpsMessageReceiver;

/**
 * Using the SPDPbuiltinParticipantReader, a local Participant
 * ‘local_participant’ discovers the existence of another Participant described
 * by the DiscoveredParticipantData participant_data. The discovered Participant
 * uses the SEDP (8.5.5.1 Discovery of a new remote Participant)
 */
public class SedpService implements Subscriber<CacheChange> {

    private static final XLogger LOGGER = XLogger.getLogger(SedpService.class);
    private RtpsTalkConfiguration config;
    private SedpBuiltinSubscriptionsReader subscriptionsReader;
    private SedpBuiltinPublicationsReader publicationsReader;
    private Subscription subscription;
    private RtpsMessageReceiver receiver;
    private boolean isStarted;

    public SedpService(RtpsTalkConfiguration config) {
        this.config = config;
        receiver = new RtpsMessageReceiver(config, "SedpServiceReceiver");
    }

    public void start(Publisher<CacheChange> participantsPublisher) throws IOException {
        LOGGER.entering("start");
        if (isStarted)
            throw new IllegalStateException("Already started");
        LOGGER.fine("Using following configuration: {0}", config);
        receiver.start(new Locator(LocatorKind.LOCATOR_KIND_UDPv4, config.builtInEnpointsPort(), config.ipAddress()),
                false);
        subscriptionsReader = new SedpBuiltinSubscriptionsReader(config.guidPrefix());
        receiver.subscribe(subscriptionsReader);
        publicationsReader = new SedpBuiltinPublicationsReader(config.guidPrefix());
        receiver.subscribe(publicationsReader);
        participantsPublisher.subscribe(this);
        isStarted = true;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(CacheChange change) {
        LOGGER.entering("onNext");
        if (change.getDataValue().serializedPayload.payload instanceof ParameterList pl) {
            configureEndpoints(change.getWriterGuid().guidPrefix, pl);
        }
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

    private void configureEndpoints(GuidPrefix guidPrefix, ParameterList participantData) {
        LOGGER.fine("Configuring builtin endpoints for guidprefix {0}", guidPrefix);
        var params = participantData.getParameters();
        var value = params.get(ParameterId.PID_BUILTIN_ENDPOINT_SET);
        if (value instanceof BuiltinEndpointSet availableEndpoints) {
            var unicast = List.<Locator>of();
            if (params.get(ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR) instanceof Locator locator) {
                unicast = List.of(locator);
            }
            configure(availableEndpoints, guidPrefix, subscriptionsReader,
                    Endpoint.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_ANNOUNCER, unicast);
            configure(availableEndpoints, guidPrefix, publicationsReader,
                    Endpoint.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_ANNOUNCER, unicast);
        } else {
            LOGGER.fine("No supported builtin endpoints, ignoring...");
        }
    }

    private void configure(BuiltinEndpointSet availableEndpoints, GuidPrefix guidPrefix,
            StatefullRtpsReader reader,
            Endpoint endpoint, List<Locator> unicast) {
        if (!availableEndpoints.hasEndpoint(endpoint)) {
            LOGGER.fine(
                    "Participant does not support {0} endpoint, ignoring...", endpoint);
            return;
        }
        reader.matchedWriterAdd(new WriterProxy(reader.getGuid(),
                new Guid(guidPrefix, endpoint.getEntityId().getValue()),
                config.packetBufferSize(),
                unicast));
    }
}
