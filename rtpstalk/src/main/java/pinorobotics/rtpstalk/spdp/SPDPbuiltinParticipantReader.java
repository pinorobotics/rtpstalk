package pinorobotics.rtpstalk.spdp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.entities.Data;
import pinorobotics.rtpstalk.entities.Guid;
import pinorobotics.rtpstalk.entities.Locator;
import pinorobotics.rtpstalk.entities.Parameter;
import pinorobotics.rtpstalk.entities.ParameterId;
import pinorobotics.rtpstalk.entities.ParameterList;
import pinorobotics.rtpstalk.entities.RtpsMessage;
import pinorobotics.rtpstalk.entities.SerializedPayload;
import pinorobotics.rtpstalk.entities.Submessage;
import pinorobotics.rtpstalk.entities.SubmessageKind.Value;
import pinorobotics.rtpstalk.io.RtpsMessageReader;

public class SPDPbuiltinParticipantReader {

	private static final XLogger LOGGER = XLogger.getLogger(SPDPbuiltinParticipantReader.class);
	private String iface = "lo";
	private ExecutorService executor = ForkJoinPool.commonPool();
	private Map<Guid, RtpsMessage> historyCache = new HashMap<>();
	private RtpsMessageReader reader = new RtpsMessageReader();
	
	public SPDPbuiltinParticipantReader withNetworkIface(String iface) {
		this.iface  = iface;
		return this;
	}
	
	public void start() throws Exception {
	     var ni = NetworkInterface.getByName(iface);
	     Locator defaultMulticastLocator = Locator.createDefaultMulticastLocator(0);
	     var dc = DatagramChannel.open(StandardProtocolFamily.INET)
	         .setOption(StandardSocketOptions.SO_REUSEADDR, true)
	         .bind(new InetSocketAddress(defaultMulticastLocator.port()))
	         .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
	     var group = InetAddress.getByName(defaultMulticastLocator.address());
	     dc.join(group, ni);
	     executor.execute(() -> {
	    	 var thread = Thread.currentThread();
	    	 LOGGER.fine("Running SPDPbuiltinParticipantReader on thread {0} with id {1}", thread.getName(),
	    			 thread.getId());
		     while (!executor.isShutdown()) {
			     try {
			    	 var buf = ByteBuffer.allocate(1024);
			    	 dc.receive(buf);
			    	 var len = buf.position();
			    	 buf.rewind();
			    	 buf.limit(len);
			    	 reader.readRtpsMessage(buf).ifPresent(this::process);
				} catch (Exception e) {
					LOGGER.severe(e);
				}
		     }
		     LOGGER.fine("Shutdown received, stopping...");
	     });
	}

	public Map<Guid, RtpsMessage> getParticipants() {
		return historyCache;
	}

	private void process(RtpsMessage message) {
		LOGGER.fine("Processing RTPS message");
		findParameterValues(message, ParameterId.PID_PARTICIPANT_GUID)
			.findFirst()
			.ifPresent(value -> {
				var guid = (Guid)value;
				if (historyCache.containsKey(guid)) {
					LOGGER.fine("Message with GUID {0} already exist", guid);
					return;
				}
				LOGGER.fine("Message with GUID {0} is new, adding it into the cache", guid);
				historyCache.put(guid, message);
			});
	}

	public static Stream<Data> findDataElements(RtpsMessage message) {
		return message.submessages().stream()
				.filter(Submessage.filterBySubmessageKind(Value.DATA))
				.map(Submessage::submessageElements)
				.flatMap(List::stream)
				.map(e -> (Data)e);
	}

	public static Stream<Object> findParameterValues(RtpsMessage message, ParameterId paramId) {
		return findDataElements(message)
			.map(SPDPbuiltinParticipantReader::findParameterList)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(ParameterList::params)
			.flatMap(List::stream)
			.filter(p -> p.parameterId() == paramId)
			.map(Parameter::value);
	}
	
	public static Optional<ParameterList> findParameterList(Data data) {
		return getParameterList(data.getSerializedPayload());
	}
	
	public static Optional<ParameterList> getParameterList(SerializedPayload payload) {
		if (payload.payload() instanceof ParameterList pl) {
			return Optional.of(pl);
		}
		return Optional.empty();
	}
	
}
