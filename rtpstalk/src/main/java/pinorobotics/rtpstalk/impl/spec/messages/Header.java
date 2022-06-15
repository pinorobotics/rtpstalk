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
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.VendorId;

/**
 * The Header identifies the message as belonging to the RTPS protocol. The Header identifies the
 * version of the protocol and the vendor that sent the message.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class Header {

    public ProtocolId protocolId;

    /** Identifies the RTPS protocol version used by the Participant. */
    public ProtocolVersion protocolVersion;

    /** Identifies the vendor of the DDS middleware that contains the Participant. */
    public VendorId vendorId;

    /**
     * The common GuidPrefix_t of the Participant and all the Endpoints contained within the
     * Participant.
     */
    public GuidPrefix guidPrefix;

    public Header() {}

    public Header(
            ProtocolId protocolId,
            ProtocolVersion protocolVersion,
            VendorId vendorId,
            GuidPrefix guidPrefix) {
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
