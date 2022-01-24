package pinorobotics.rtpstalk.discovery.sedp;

import id.xfunction.logging.XLogger;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.reader.WriterProxy;
import pinorobotics.rtpstalk.discovery.spdp.SpdpBuiltinParticipantReader;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet.Endpoint;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.structure.CacheChange;

/**
 * Using the SPDPbuiltinParticipantReader, a local Participant
 * ‘local_participant’ discovers the existence of another Participant described
 * by the DiscoveredParticipantData participant_data. The discovered Participant
 * uses the SEDP (8.5.5.1 Discovery of a new remote Participant)
 */
public class SedpService implements Subscriber<CacheChange> {

    private static final XLogger LOGGER = XLogger.getLogger(SedpService.class);
    private RtpsTalkConfiguration config = RtpsTalkConfiguration.DEFAULT;
    private SpdpBuiltinParticipantReader spdpReader;
    private SedpBuiltinPublicationsReader sedpReader;
    private Subscription subscription;

    public SedpService(SpdpBuiltinParticipantReader reader) {
        this.spdpReader = reader;
    }

    public SedpService withRtpsTalkConfiguration(RtpsTalkConfiguration config) {
        this.config = config;
        return this;
    }

    public void start() throws IOException {
        LOGGER.entering("start");
        LOGGER.fine("Using following configuration: {0}", config);
        var dataChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                .bind(new InetSocketAddress(config.ipAddress(), config.builtInEnpointsPort()));
        new SedpBuiltinPublicationsReader(
                dataChannel,
                config.packetBufferSize()).start();
        spdpReader.getCache().subscribe(this);
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
        LOGGER.fine("Configuring builtin endpoints");
        var params = participantData.getParameters();
        var value = params.get(ParameterId.PID_BUILTIN_ENDPOINT_SET);
        if (value instanceof BuiltinEndpointSet availableEndpoints) {
            if (!availableEndpoints.hasEndpoint(Endpoint.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_ANNOUNCER)) {
                LOGGER.fine(
                        "Participant does not support BUILTIN_ENDPOINT_PUBLICATIONS_ANNOUNCER endpoint, ignoring...");
                return;
            }
            var unicast = List.<Locator>of();
            if (params.get(ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR) instanceof Locator locator) {
                unicast = List.of(locator);
            }
            sedpReader.matchedWriterAdd(new WriterProxy(
                    new Guid(guidPrefix, EntityId.Predefined.ENTITYID_SEDP_BUILTIN_PUBLICATIONS_ANNOUNCER.getValue()),
                    unicast));
        } else {
            LOGGER.fine("No supported builtin endpoints, ignoring...");
        }
    }
}
