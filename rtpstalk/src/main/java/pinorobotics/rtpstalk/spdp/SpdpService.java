package pinorobotics.rtpstalk.spdp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.util.List;

import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.entities.GuidPrefix;
import pinorobotics.rtpstalk.entities.Header;
import pinorobotics.rtpstalk.entities.InfoTimestamp;
import pinorobotics.rtpstalk.entities.Locator;
import pinorobotics.rtpstalk.entities.ProtocolId;
import pinorobotics.rtpstalk.entities.ProtocolVersion;
import pinorobotics.rtpstalk.entities.RtpsMessage;
import pinorobotics.rtpstalk.entities.Submessage;
import pinorobotics.rtpstalk.entities.SubmessageHeader;
import pinorobotics.rtpstalk.entities.SubmessageKind;
import pinorobotics.rtpstalk.entities.VendorId;

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
		writer =new SpdpBuiltinParticipantWriter(dc);
		reader.start();
		writer.start();
	}

	@Override
	public void close() throws Exception {
		writer.close();
	}
	
	private RtpsMessage createSpdpDiscoveredParticipantData() {
		List<Submessage> submessages;
		Header header = new Header(
				ProtocolId.Predefined.RTPS.getValue(),
				ProtocolVersion.Predefined.Version_2_3.getValue(),
				VendorId.Predefined.RTPSTALK.getValue(),
				GuidPrefix.generate());
		
		return new RtpsMessage(header, List.of(newInfoTimestampSubmessage()));
	}
	
	private Submessage newInfoTimestampSubmessage() {
		var header = new SubmessageHeader(SubmessageKind.Predefined.INFO_TS.getValue(), 0, 123);
		return new Submessage(header, List.of(InfoTimestamp.now()));
	}
}
