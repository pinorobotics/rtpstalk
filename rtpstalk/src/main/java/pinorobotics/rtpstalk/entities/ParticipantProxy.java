package pinorobotics.rtpstalk.entities;

import java.util.Arrays;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class ParticipantProxy {

	/**
	 * Identifies the DDS domainId of the associated DDS DomainParticipant.
	 */
	@Streamed
	public int domainId;
	
	/**
	 * Identifies the DDS domainTag of the associated DDS DomainParticipant.
	 */
	@Streamed
	public String domainTag;
	
	/**
	 * Identifies the RTPS protocol version used by the Participant.
	 */
	@Streamed
	public ProtocolVersion protocolVersion;
	
	/**
	 * The common GuidPrefix_t of the Participant and all the
	 * Endpoints contained within the Participant.
	 */
	@Streamed
	public GuidPrefix guidPrefix;
	
	/**
	 * Identifies the vendor of the DDS middleware that contains
	 * the Participant.
	 */
	@Streamed
	public VendorId vendorId;
	
	/**
	 * Describes whether the Readers within the Participant
	 * expect that the QoS values that apply to each data
	 * modification are encapsulated included with each Data.
	 */
	@Streamed
	public boolean expectsInlineQos;
	
	@Streamed
	public BuiltinEndpointSet availableBuiltinEndpoints;

	/**
	 * Provides additional information on the QoS of the built-in
	 * Endpoints supported by the Participant
	 */
	@Streamed
	public BuiltinEndpointQos builtinEndpointQos;
	
	/**
	 * List of unicast locators (transport, address, port
	 * combinations) that can be used to send messages to the
	 * built-in Endpoints contained in the Participant.
	 */
	@Streamed
	public Locator[] metatrafficUnicastLocatorList = new Locator[0];
	
	/**
	 * List of multicast locators (transport, address, port
	 * combinations) that can be used to send messages to the
	 * built-in Endpoints contained in the Participant.
	 */
	//@Streamed
	public Locator[] metatrafficMulticastLocatorList = new Locator[0];

	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("domainId", domainId);
		builder.append("domainTag", domainTag);
		builder.append("protocolVersion", protocolVersion);
		builder.append("guidPrefix", guidPrefix);
		builder.append("vendorId", vendorId);
		builder.append("expectsInlineQos", expectsInlineQos);
		builder.append("availableBuiltinEndpoints", availableBuiltinEndpoints);
		builder.append("builtinEndpointQos", builtinEndpointQos);
		builder.append("metatrafficUnicastLocatorList", Arrays.toString(metatrafficUnicastLocatorList));
		builder.append("metatrafficMulticastLocatorList", Arrays.toString(metatrafficMulticastLocatorList));
		return builder.toString();
	}
	
	
/*
 *  

defaultUnicastLocatorList Locator_t[1..*] Default list of unicast locators (transport, address, port
combinations) that can be used to send messages to the
user-defined Endpoints contained in the Participant.
These are the unicast locators that will be used in case the
Endpoint does not specify its own set of Locators, so at
least one Locator must be present.
defaultMulticastLocatorList Locator_t[*] Default list of multicast locators (transport, address, port
combinations) that can be used to send messages to the
user-defined Endpoints contained in the Participant.
These are the multicast locators that will be used in case
the Endpoint does not specify its own set of Locators

 */
}
