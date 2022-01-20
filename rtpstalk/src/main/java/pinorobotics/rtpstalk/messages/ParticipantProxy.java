package pinorobotics.rtpstalk.messages;

import java.util.Arrays;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import id.xfunction.XJsonStringBuilder;

public class ParticipantProxy {

    /**
     * Identifies the DDS domainId of the associated DDS DomainParticipant.
     */
    public int domainId;

    /**
     * Identifies the DDS domainTag of the associated DDS DomainParticipant.
     */
    public String domainTag;

    /**
     * Identifies the RTPS protocol version used by the Participant.
     */
    public ProtocolVersion protocolVersion;

    /**
     * The common GuidPrefix_t of the Participant and all the Endpoints contained
     * within the Participant.
     */
    public GuidPrefix guidPrefix;

    /**
     * Identifies the vendor of the DDS middleware that contains the Participant.
     */
    public VendorId vendorId;

    /**
     * Describes whether the Readers within the Participant expect that the QoS
     * values that apply to each data modification are encapsulated included with
     * each Data.
     */
    public boolean expectsInlineQos;

    public BuiltinEndpointSet availableBuiltinEndpoints;

    /**
     * Provides additional information on the QoS of the built-in Endpoints
     * supported by the Participant
     */
    public BuiltinEndpointQos builtinEndpointQos;

    /**
     * List of unicast locators (transport, address, port combinations) that can be
     * used to send messages to the built-in Endpoints contained in the Participant.
     */
    public Locator[] metatrafficUnicastLocatorList;

    /**
     * List of multicast locators (transport, address, port combinations) that can
     * be used to send messages to the built-in Endpoints contained in the
     * Participant.
     */
    public Locator[] metatrafficMulticastLocatorList;

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

}
