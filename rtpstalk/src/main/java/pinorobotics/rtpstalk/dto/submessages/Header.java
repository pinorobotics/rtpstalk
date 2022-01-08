package pinorobotics.rtpstalk.dto.submessages;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.dto.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.dto.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.dto.submessages.elements.VendorId;

/**
 * The Header identifies the message as belonging to the RTPS protocol.
 * The Header identifies the version of the protocol
 * and the vendor that sent the message. 
 */
public class Header {

	public ProtocolId protocolId;

	/**
	 * Identifies the RTPS protocol version used by the Participant.
	 */
	public ProtocolVersion protocolVersion;

	/**
	 * Identifies the vendor of the DDS middleware that contains
	 * the Participant.
	 */
	public VendorId vendorId;

	/**
	 * The common GuidPrefix_t of the Participant and all the
	 * Endpoints contained within the Participant.
	 */
	public GuidPrefix guidPrefix;

	public Header() {

	}

	public Header(ProtocolId protocolId, ProtocolVersion protocolVersion, VendorId vendorId, GuidPrefix guidPrefix) {
		this.protocolId = protocolId;
		this.protocolVersion = protocolVersion;
		this.vendorId = vendorId;
		this.guidPrefix = guidPrefix;
	}

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
