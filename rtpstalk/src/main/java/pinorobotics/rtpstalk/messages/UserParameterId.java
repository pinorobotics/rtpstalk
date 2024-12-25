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

import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;

/**
 * List of user parameters.
 *
 * <p>These parameters are not part of RTPS specification.
 *
 * <p>These parameters include DDS, or any vendor specific parameters which are supported by
 * <b>rtpstalk</b>.
 */
public interface UserParameterId {

    /**
     * FastDDS legacy implementation of PID_SAMPLE_IDENTITY
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
