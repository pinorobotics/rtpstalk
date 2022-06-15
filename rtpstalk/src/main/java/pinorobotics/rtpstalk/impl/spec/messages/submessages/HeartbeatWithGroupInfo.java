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
package pinorobotics.rtpstalk.impl.spec.messages.submessages;

import pinorobotics.rtpstalk.impl.spec.messages.GroupDigest;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class HeartbeatWithGroupInfo extends Heartbeat {

    /**
     * Present only if the GroupInfo flag is set in the header. Identifies the last (highest) group
     * sequence number written by any DataWriter in the Writer?????????s Group at the time that the
     * HeartBeat was sent.
     */
    public SequenceNumber currentGSN;

    /**
     * Present only if the GroupInfo flag is set in the header. Identifies the group sequence number
     * corresponding to the sample identified by sequence number firstSN.
     */
    public SequenceNumber firstGSN;

    /**
     * Present only if the GroupInfo flag is set in the header. Identifies the group sequence number
     * corresponding to the sample identified by sequence number lastSN.
     */
    public SequenceNumber lastGSN;

    /**
     * Present only if the GroupInfo flag is set in the header. Identifies the subset of Writers
     * that belong to the Writer?????????s Group at the time the sample with currentGSN was written.
     * secureWriterSet GroupDigest Present only if the GroupInfoFlag is set in the header. Reserved
     * for use by the DDS-Security Specification.
     */
    public GroupDigest writerSet;

    public HeartbeatWithGroupInfo() {}

    @Override
    protected Object[] getAdditionalFields() {
        var superFields = super.getAdditionalFields();
        var fields =
                new Object[] {
                    "currentGSN", currentGSN,
                    "firstGSN", firstGSN,
                    "lastGSN", lastGSN,
                    "writerSet", writerSet
                };
        var res = new Object[superFields.length + fields.length];
        System.arraycopy(superFields, 0, res, 0, superFields.length);
        System.arraycopy(fields, 0, res, superFields.length, fields.length);
        return res;
    }
}
