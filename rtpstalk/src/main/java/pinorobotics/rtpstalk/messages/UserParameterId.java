/*
 * Copyright 2024 pinorobotics
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
package pinorobotics.rtpstalk.messages;

import pinorobotics.rtpstalk.impl.spec.DdsSpecReference;
import pinorobotics.rtpstalk.impl.spec.DdsVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;

/**
 * List of user parameters.
 *
 * <p>These parameters include DDS, or any vendor specific parameters which are supported by
 * <b>rtpstalk</b> but they are not part of RTPS specification.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 * @author lambdaprime intid@protonmail.com
 */
public interface UserParameterId {

    /**
     * <b>rtpstalk</b> provides RTPS level support for {@link #PID_RELATED_SAMPLE_IDENTITY} (see
     * {@link DdsVersion#DDS_RPC_1_0}) which allows users to implement DDS-RPC.
     *
     * <p>The support includes following:
     *
     * <ul>
     *   <li>Replace {@link SequenceNumber#SEQUENCENUMBER_UNKNOWN} inside sample identity with
     *       actual sequence number of the incoming RTPS message
     *   <li>Replace any outgoing DATA submessages with GAP submessages, when sample identity inside
     *       DATA submessages does not match the recipient.
     * </ul>
     *
     * @see <a href="https://github.com/pinorobotics/jros2services">Example of DDS-RPC
     *     implementation with <b>rtpstalk</b></a>
     */
    @DdsSpecReference(
            paragraph = "7.8.2 Request and Reply Correlation in the Enhanced Service Profile",
            protocolVersion = DdsVersion.DDS_RPC_1_0)
    short PID_RELATED_SAMPLE_IDENTITY = (short) 0x0083;

    /**
     * FastDDS legacy implementation of {@link #PID_RELATED_SAMPLE_IDENTITY}
     *
     * <p>sample_identity is an extension for requester-replier configuration. It contains the
     * DataWriter and the sequence number of the current message, and it is used by the replier to
     * fill the related_sample_identity when it sends the reply [<a
     * href="https://fast-dds.docs.eprosima.com/en/latest/fastdds/dds_layer/subscriber/sampleInfo/sampleInfo.html#dds-layer-subscriber-sampleinfo-sampleidentity">FastDDS
     * 3.1.0 documentation</a>
     *
     * <p>When Reader receives {@link #PID_FASTDDS_SAMPLE_IDENTITY} as part of inlineQos and if
     * identity sequenceNumber is {@link SequenceNumber#SEQUENCENUMBER_UNKNOWN} then Reader will
     * replace such sequenceNumber with the sequenceNumber of the RTPS message
     *
     * @see <a
     *     href="https://github.com/eProsima/Fast-DDS/blob/330add882f9db1460ad47b32d98a2b5be608ad0f/include/fastdds/dds/core/policy/ParameterTypes.hpp#L175">PID_CUSTOM_RELATED_SAMPLE_IDENTITY</a>
     */
    short PID_FASTDDS_SAMPLE_IDENTITY = (short) 0x800f;
}
