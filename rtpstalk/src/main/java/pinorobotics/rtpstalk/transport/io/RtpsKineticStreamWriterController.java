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
package pinorobotics.rtpstalk.transport.io;

import id.kineticstreamer.KineticStreamWriterController;
import id.kineticstreamer.OutputKineticStream;
import pinorobotics.rtpstalk.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumberSet;

class RtpsKineticStreamWriterController extends KineticStreamWriterController {

    @Override
    public Result onNextObject(OutputKineticStream in, Object obj) throws Exception {
        var rtpsStream = (RtpsOutputKineticStream) in;
        if (obj instanceof ParameterList pl) {
            // writing it manually since we convert it to custom type
            rtpsStream.writeParameterList(pl);
            return new Result(true);
        }
        if (obj instanceof SequenceNumber num) {
            // writing it manually in custom format
            rtpsStream.writeSequenceNumber(num);
            return new Result(true);
        }
        if (obj instanceof SequenceNumberSet set) {
            // writing it manually in custom format
            rtpsStream.writeSequenceNumberSet(set);
            return new Result(true);
        }
        return Result.CONTINUE;
    }
}
