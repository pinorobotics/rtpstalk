package pinorobotics.rtpstalk.spdp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.util.List;

import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.dto.submessages.Header;
import pinorobotics.rtpstalk.dto.submessages.InfoTimestamp;
import pinorobotics.rtpstalk.dto.submessages.Locator;
import pinorobotics.rtpstalk.dto.submessages.ProtocolId;
import pinorobotics.rtpstalk.dto.submessages.RtpsMessage;
import pinorobotics.rtpstalk.dto.submessages.Submessage;
import pinorobotics.rtpstalk.dto.submessages.SubmessageHeader;
import pinorobotics.rtpstalk.dto.submessages.SubmessageKind;
import pinorobotics.rtpstalk.dto.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.dto.submessages.elements.Parameter;
import pinorobotics.rtpstalk.dto.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.dto.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.dto.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.dto.submessages.elements.VendorId;

public class SpdpService implements AutoCloseable {

	private RtpsTalkConfiguration config = RtpsTalkConfiguration.DEFAULT;
	private SpdpBuiltinParticipantReader reader;
	private SpdpBuiltinParticipantWriter writer;

	public SpdpService withRtpsTalkConfiguration(RtpsTalkConfiguration config) {
		this.config = config;
		return this;
	}

	public void start() throws Exception {
		var ni = NetworkInterface.getByName(config.networkIface());
		Locator defaultMulticastLocator = Locator.createDefaultMulticastLocator(0);
		var dc = DatagramChannel.open(StandardProtocolFamily.INET)
				.setOption(StandardSocketOptions.SO_REUSEADDR, true)
				.bind(new InetSocketAddress(defaultMulticastLocator.port()))
				.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
		var group = InetAddress.getByName(defaultMulticastLocator.address());
		dc.join(group, ni);
		reader = new SpdpBuiltinParticipantReader(dc);
		writer = new SpdpBuiltinParticipantWriter(dc);
//		writer.setSpdpDiscoveredParticipantData(createSpdpDiscoveredParticipantData());
		reader.start();
//		writer.start();
	}

	@Override
	public void close() throws Exception {
		writer.close();
	}
	
//	private RtpsMessage createSpdpDiscoveredParticipantData() {
//		List<Submessage> submessages;
//		var guidPrefix = GuidPrefix.generate();
//		Header header = new Header(
//				ProtocolId.Predefined.RTPS.getValue(),
//				ProtocolVersion.Predefined.Version_2_3.getValue(),
//				VendorId.Predefined.RTPSTALK.getValue(),
//				guidPrefix);
//		var params = List.of(new Parameter(ParameterId.PID_PARTICIPANT_GUID, guidPrefix));
//		return new RtpsMessage(header, List.of(newInfoTimestampSubmessage(),
//				newDataSubmessage(new ParameterList(params))));
//	}
//	
//	private Submessage newDataSubmessage(ParameterList parameterList) {
//		var header = new SubmessageHeader(SubmessageKind.Predefined.DATA.getValue(), 0, 123);
//		return new Submessage(header, List.of(InfoTimestamp.now()));
//	}
//
//	private Submessage newInfoTimestampSubmessage() {
//		var header = new SubmessageHeader(SubmessageKind.Predefined.INFO_TS.getValue(), 0, 123);
//		return new Submessage(header, List.of(InfoTimestamp.now()));
//	}
}
