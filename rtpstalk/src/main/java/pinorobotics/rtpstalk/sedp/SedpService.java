package pinorobotics.rtpstalk.sedp;

import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.io.RtpcInputKineticStream;
import pinorobotics.rtpstalk.io.RtpsMessageReader;

public class SedpService implements AutoCloseable {

	private static final XLogger LOGGER = XLogger.getLogger(RtpcInputKineticStream.class);
	private RtpsTalkConfiguration config = RtpsTalkConfiguration.DEFAULT;
	private ExecutorService executor = ForkJoinPool.commonPool();
	private RtpsMessageReader reader = new RtpsMessageReader();
	private DatagramChannel dataChannel;

	public SedpService withRtpsTalkConfiguration(RtpsTalkConfiguration config) {
		this.config = config;
		return this;
	}

	public void start() throws Exception {
		LOGGER.entering("start");
		LOGGER.fine("Using following configuration: {0}", config);
		dataChannel = DatagramChannel.open(StandardProtocolFamily.INET)
				.bind(new InetSocketAddress(config.ipAddress(), config.builtInEnpointsPort()));
	     executor.execute(() -> {
	    	 var thread = Thread.currentThread();
	    	 LOGGER.fine("Running SPDPbuiltinParticipantReader on thread {0} with id {1}", thread.getName(),
	    			 thread.getId());
		     while (!executor.isShutdown()) {
		    	 try {
					var buf = ByteBuffer.allocate(config.packetBufferSize());
					var addr = dataChannel.receive(buf);
					buf.limit(buf.position());
					buf.rewind();
					System.out.println(reader.readRtpsMessage(buf));
		    	 } catch (Exception e) {
		    		 e.printStackTrace();
		    	 }
		     }
		     LOGGER.fine("Shutdown received, stopping...");
	     });
	}

	@Override
	public void close() throws Exception {
		dataChannel.close();
		executor.shutdown();
	}
}
