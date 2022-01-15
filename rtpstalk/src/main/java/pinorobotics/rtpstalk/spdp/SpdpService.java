package pinorobotics.rtpstalk.spdp;

import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.util.EnumSet;
import java.util.List;

import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.dto.BuiltinEndpointSet;
import pinorobotics.rtpstalk.dto.Duration;
import pinorobotics.rtpstalk.dto.Guid;
import pinorobotics.rtpstalk.dto.Header;
import pinorobotics.rtpstalk.dto.Locator;
import pinorobotics.rtpstalk.dto.LocatorKind;
import pinorobotics.rtpstalk.dto.ProtocolId;
import pinorobotics.rtpstalk.dto.RtpsMessage;
import pinorobotics.rtpstalk.dto.BuiltinEndpointSet.Flags;
import pinorobotics.rtpstalk.dto.submessages.Data;
import pinorobotics.rtpstalk.dto.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.dto.submessages.RepresentationIdentifier;
import pinorobotics.rtpstalk.dto.submessages.SerializedPayload;
import pinorobotics.rtpstalk.dto.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.dto.submessages.Submessage;
import pinorobotics.rtpstalk.dto.submessages.elements.EntityId;
import pinorobotics.rtpstalk.dto.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.dto.submessages.elements.Parameter;
import pinorobotics.rtpstalk.dto.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.dto.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.dto.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.dto.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.dto.submessages.elements.VendorId;
import pinorobotics.rtpstalk.io.RtpsInputKineticStream;

public class SpdpService implements AutoCloseable {

	private static final XLogger LOGGER = XLogger.getLogger(RtpsInputKineticStream.class);
	private RtpsTalkConfiguration config = RtpsTalkConfiguration.DEFAULT;
	private SpdpBuiltinParticipantReader reader;
	private SpdpBuiltinParticipantWriter writer;
	private DatagramChannel dataChannel;

	public SpdpService withRtpsTalkConfiguration(RtpsTalkConfiguration config) {
		this.config = config;
		return this;
	}

	public void start() throws Exception {
		LOGGER.entering("start");
		LOGGER.fine("Using following configuration: {0}", config);
		var ni = NetworkInterface.getByName(config.networkIface());
		Locator defaultMulticastLocator = Locator.createDefaultMulticastLocator(0);
		dataChannel = DatagramChannel.open(StandardProtocolFamily.INET)
				.setOption(StandardSocketOptions.SO_REUSEADDR, true)
				.bind(new InetSocketAddress(defaultMulticastLocator.port()))
				.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
		var group = defaultMulticastLocator.address();
		dataChannel.join(group, ni);
		reader = new SpdpBuiltinParticipantReader(dataChannel, config.packetBufferSize());
		writer = new SpdpBuiltinParticipantWriter(dataChannel, config.packetBufferSize(), group);
		writer.setSpdpDiscoveredParticipantData(createSpdpDiscoveredParticipantData());
		reader.start();
		writer.start();
	}

	@Override
	public void close() throws Exception {
		dataChannel.close();
		writer.close();
	}
	
	private RtpsMessage createSpdpDiscoveredParticipantData() {
		var guidPrefix = GuidPrefix.generate();
		var params = List.of(
				new Parameter(ParameterId.PID_PROTOCOL_VERSION, ProtocolVersion.Predefined.Version_2_3.getValue()),
				new Parameter(ParameterId.PID_VENDORID, VendorId.Predefined.RTPSTALK.getValue()),
				new Parameter(ParameterId.PID_PARTICIPANT_GUID, new Guid(
						guidPrefix, EntityId.Predefined.ENTITYID_PARTICIPANT.getValue())),
				new Parameter(ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR, new Locator(
						LocatorKind.LOCATOR_KIND_UDPv4, config.builtInEnpointsPort(), config.ipAddress())),
				new Parameter(ParameterId.PID_DEFAULT_UNICAST_LOCATOR, new Locator(
						LocatorKind.LOCATOR_KIND_UDPv4, config.userEndpointsPort(), config.ipAddress())),
				new Parameter(ParameterId.PID_PARTICIPANT_LEASE_DURATION, new Duration(20)),
				new Parameter(ParameterId.PID_BUILTIN_ENDPOINT_SET, new BuiltinEndpointSet(EnumSet.of(
						Flags.DISC_BUILTIN_ENDPOINT_PARTICIPANT_ANNOUNCER,
						Flags.DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR,
						Flags.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_ANNOUNCER,
						Flags.DISC_BUILTIN_ENDPOINT_PUBLICATIONS_DETECTOR,
						Flags.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_ANNOUNCER,
						Flags.DISC_BUILTIN_ENDPOINT_SUBSCRIPTIONS_DETECTOR,
						Flags.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_WRITER,
						Flags.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER,
						Flags.SECURE_PUBLICATION_WRITER,
						Flags.SECURE_PUBLICATION_READER,
						Flags.PARTICIPANT_SECURE_READER,
						Flags.SECURE_SUBSCRIPTION_WRITER,
						Flags.SECURE_SUBSCRIPTION_READER,
						Flags.SECURE_PARTICIPANT_MESSAGE_WRITER,
						Flags.SECURE_PARTICIPANT_MESSAGE_READER,
						Flags.PARTICIPANT_SECURE_WRITER))),
				new Parameter(ParameterId.PID_ENTITY_NAME, "/")
		);
		var submessages = new Submessage[] {InfoTimestamp.now(),
				new Data(0b101, 0,
					EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_DETECTOR.getValue(),
					EntityId.Predefined.ENTITYID_SPDP_BUILTIN_PARTICIPANT_ANNOUNCER.getValue(),
					new SequenceNumber(0, 1),
					new SerializedPayload(new SerializedPayloadHeader(
							RepresentationIdentifier.Predefined.PL_CDR_LE.getValue()),
							new ParameterList(params)))};
		Header header = new Header(
				ProtocolId.Predefined.RTPS.getValue(),
				ProtocolVersion.Predefined.Version_2_3.getValue(),
				VendorId.Predefined.RTPSTALK.getValue(),
				guidPrefix);
		return new RtpsMessage(header, submessages);
	}
	
}
