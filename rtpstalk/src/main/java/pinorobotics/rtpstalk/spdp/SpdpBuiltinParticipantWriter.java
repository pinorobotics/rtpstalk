package pinorobotics.rtpstalk.spdp;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.entities.RtpsMessage;
import pinorobotics.rtpstalk.io.RtpsMessageWriter;

public class SpdpBuiltinParticipantWriter implements Runnable, AutoCloseable {

	private static final XLogger LOGGER = XLogger.getLogger(SpdpBuiltinParticipantWriter.class);
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
			new NamedThreadFactory("SPDPbuiltinParticipantWriter"));
	private RtpsMessageWriter writer = new RtpsMessageWriter();
	private DatagramChannel dc;
	private RtpsMessage data;
	
	public SpdpBuiltinParticipantWriter(DatagramChannel dc) {
		this.dc = dc;
	}

	public void start() throws Exception {
	     executor.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
	}

	public void setSpdpDiscoveredParticipantData(RtpsMessage data) {
		this.data = data;
	}
	
	@Override
	public void run() {
		if (executor.isShutdown()) return;
		var thread = Thread.currentThread();
		LOGGER.fine("Running SPDPbuiltinParticipantWriter on thread {0} with id {1}", thread.getName(),
				thread.getId());
		if (data == null) {
			LOGGER.fine("No SpdpDiscoveredParticipantData to send, skipping");
			return;
		}
		var buf = writer.writeRtpsMessage(data);
		try {
			dc.write(buf);
		} catch (IOException e) {
			LOGGER.severe(e);
			return;
		}
		LOGGER.fine("Sent SpdpDiscoveredParticipantData");
	}
	
	@Override
	public void close() throws Exception {
		executor.shutdown();
	}

	
}
