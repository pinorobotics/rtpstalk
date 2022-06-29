/*
 * Copyright 2022 rtpstalk project
 * 
 * Website: https://github.com/pinorobotics/rtpstalk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pinorobotics.rtpstalk.impl.spec.messages;

import id.xfunction.XJsonStringBuilder;
import java.util.Arrays;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ParticipantProxy {

    /** Identifies the DDS domainId of the associated DDS DomainParticipant. */
    public int domainId;

    /** Identifies the DDS domainTag of the associated DDS DomainParticipant. */
    public String domainTag;

    /** Identifies the RTPS protocol version used by the Participant. */
    public ProtocolVersion protocolVersion;

    /**
     * The common GuidPrefix_t of the Participant and all the Endpoints contained within the
     * Participant.
     */
    public GuidPrefix guidPrefix;

    /** Identifies the vendor of the DDS middleware that contains the Participant. */
    public VendorId vendorId;

    /**
     * Describes whether the Readers within the Participant expect that the QoS values that apply to
     * each data modification are encapsulated included with each Data.
     */
    public boolean expectsInlineQos;

    public BuiltinEndpointSet availableBuiltinEndpoints;

    /**
     * Provides additional information on the QoS of the built-in Endpoints supported by the
     * Participant
     */
    public BuiltinEndpointQos builtinEndpointQos;

    /**
     * List of unicast locators (transport, address, port combinations) that can be used to send
     * messages to the built-in Endpoints contained in the Participant.
     */
    public Locator[] metatrafficUnicastLocatorList;

    /**
     * List of multicast locators (transport, address, port combinations) that can be used to send
     * messages to the built-in Endpoints contained in the Participant.
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
        builder.append(
                "metatrafficUnicastLocatorList", Arrays.toString(metatrafficUnicastLocatorList));
        builder.append(
                "metatrafficMulticastLocatorList",
                Arrays.toString(metatrafficMulticastLocatorList));
        return builder.toString();
    }
}
