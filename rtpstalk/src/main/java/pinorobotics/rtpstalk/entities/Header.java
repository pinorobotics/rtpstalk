package pinorobotics.rtpstalk.entities;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

/**
 * The Header identifies the message as belonging to the RTPS protocol.
 * The Header identifies the version of the protocol
 * and the vendor that sent the message. 
 */
public class Header {

	@Streamed
	public ProtocolId protocolId = new ProtocolId(new byte[0]);
	
	/**
	 * Identifies the RTPS protocol version used by the Participant.
	 */
	@Streamed
	public ProtocolVersion protocolVersion;
	
	/**
	 * Identifies the vendor of the DDS middleware that contains
	 * the Participant.
	 */
	@Streamed
	public VendorId vendorId;
	
	/**
	 * The common GuidPrefix_t of the Participant and all the
	 * Endpoints contained within the Participant.
	 */
	@Streamed
	public GuidPrefix guidPrefix;
	
	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("protocolId", protocolId);
		builder.append("protocolVersion", protocolVersion);
		builder.append("vendorId", vendorId);
		builder.append("guidPrefix", guidPrefix);
		return builder.toString();
	}
}
