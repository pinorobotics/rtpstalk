/*
 * Copyright 2023 pinorobotics
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
package pinorobotics.rtpstalk.tests.behavior.reader;

import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataFragGenerator {

    private EntityId readerId;
    private EntityId writerId;
    private SequenceNumber writerSN;
    private Optional<ParameterList> inlineQos;
    private int fragmentSize;
    private int dataSize;

    public DataFragGenerator(
            EntityId readerId,
            EntityId writerId,
            SequenceNumber writerSN,
            int fragmentSize,
            int dataSize,
            Optional<ParameterList> inlineQos) {
        this.readerId = readerId;
        this.writerId = writerId;
        this.writerSN = writerSN;
        this.fragmentSize = fragmentSize;
        this.dataSize = dataSize;
        this.inlineQos = inlineQos;
    }

    public DataFrag generate(
            long fragmentStartingNum,
            int fragmentsInSubmessage,
            SerializedPayload serializedPayload) {
        return new DataFrag(
                readerId,
                writerId,
                writerSN,
                fragmentStartingNum,
                fragmentsInSubmessage,
                fragmentSize,
                dataSize,
                inlineQos,
                serializedPayload);
    }
}
