package pinorobotics.rtpstalk.discovery.spdp;

import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.messages.BuiltinEndpointSet.Endpoint;
import pinorobotics.rtpstalk.messages.Duration;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.LocatorKind;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.Data;
import pinorobotics.rtpstalk.messages.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.messages.submessages.RepresentationIdentifier;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.messages.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.transport.io.RtpsInputKineticStream;

public class SpdpService implements AutoCloseable {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsInputKineticStream.class);
    private RtpsTalkConfiguration config;
    private SpdpBuiltinParticipantReader reader;
    private SpdpBuiltinParticipantWriter writer;
    private DatagramChannel dataChannel;

    public SpdpService(RtpsTalkConfiguration config) {
        this.config = config;
    }

    public void start() throws Exception {
        LOGGER.entering("start");
        LOGGER.fine("Using following configuration: {0}", config);
        var ni = NetworkInterface.getByName(config.networkIface());
        Locator defaultMulticastLocator = Locator.createDefaultMulticastLocator(config.domainId());
        dataChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .bind(new InetSocketAddress(defaultMulticastLocator.port()))
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
        var group = defaultMulticastLocator.address();
        dataChannel.join(group, ni);
        reader = new SpdpBuiltinParticipantReader(config.guidPrefix(), dataChannel, config.packetBufferSize());
        writer = new SpdpBuiltinParticipantWriter(dataChannel, config.packetBufferSize(), group);
        writer.setSpdpDiscoveredParticipantData(createSpdpDiscoveredParticipantData());
        reader.start();
        writer.start();
    }

    public SpdpBuiltinParticipantReader getReader() {
        XAsserts.assertNotNull(reader, "Reader is not ready, service is not started");
        return reader;
    }

    @Override
    public void close() throws Exception {
        dataChannel.close();
        writer.close();
    }

    private RtpsMessage createSpdpDiscoveredParticipantData() {
        var params = List.<Entry<ParameterId, Object>>of(
                Map.entry(ParameterId.PID_PROTOCOL_VERSION, ProtocolVersion.Predefined.Version_2_3.getValue()),
                Map.entry(ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue()),
                Map.entry(ParameterId.PID_PARTICIPANT_GUID, new Guid(
                        config.guidPrefix(), EntityId.Predefined.ENTITYID_PARTICIPANT.getValue())),
                Map.entry(ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR, new Locator(
                        LocatorKind.LOCATOR_KIND_UDPv4, config.builtInEnpointsPort(), config.ipAddress())),
                Map.entry(ParameterId.PID_DEFAULT_UNICAST_LOCATOR, new Locator(
                        LocatorKind.LOCATOR_KIND_UDPv4, config.userEndpointsPort(), config.ipAddress())),
                Map.entry(ParameterId.PID_PARTICIPANT_LEASE_DURATION, new Duration(20)),
                Map.entry(ParameterId.PID_BUILTIN_ENDPOINT_SET, new BuiltinEndpointSet(EnumSet.of(
                        Endpoint.DISC_BUILTIN_ENDPOINT_PARTICIPANT_ANNOUNCER,
                        Endpoint.DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR,
                        Endpoint.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_ANNOUNCER,
                        Endpoint.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_DETECTOR,
                        Endpoint.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_ANNOUNCER,
                        Endpoint.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_DETECTOR,
                        Endpoint.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_WRITER,
                        Endpoint.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER,
                        Endpoint.SECURE_PUBLICATION_WRITER,
                        Endpoint.SECURE_PUBLICATION_READER,
                        Endpoint.PARTICIPANT_SECURE_READER,
                        Endpoint.SECURE_SUBSCRIPTION_WRITER,
                        Endpoint.SECURE_SUBSCRIPTION_READER,
                        Endpoint.SECURE_PARTICIPANT_MESSAGE_WRITER,
                        Endpoint.SECURE_PARTICIPANT_MESSAGE_READER,
                        Endpoint.PARTICIPANT_SECURE_WRITER))),
                Map.entry(ParameterId.PID_ENTITY_NAME, "/"));
        var submessages = new Submessage[] { InfoTimestamp.now(),
                new Data(0b100 | RtpsTalkConfiguration.ENDIANESS_BIT, 0,
                        EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue(),
                        EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_ANNOUNCER.getValue(),
                        new SequenceNumber(1),
                        new SerializedPayload(new SerializedPayloadHeader(
                                RepresentationIdentifier.Predefined.PL_CDR_LE.getValue()),
                                new ParameterList(params))) };
        Header header = new Header(
                ProtocolId.Predefined.RTPS.getValue(),
                ProtocolVersion.Predefined.Version_2_3.getValue(),
                VendorId.Predefined.RTPSTALK.getValue(),
                config.guidPrefix());
        return new RtpsMessage(header, submessages);
    }

}
