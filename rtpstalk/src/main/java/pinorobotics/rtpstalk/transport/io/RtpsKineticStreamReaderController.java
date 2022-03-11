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

import id.kineticstreamer.InputKineticStream;
import id.kineticstreamer.KineticStreamReaderController;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumberSet;

/** @author aeon_flux aeon_flux@eclipso.ch */
class RtpsKineticStreamReaderController extends KineticStreamReaderController {

    @Override
    public Result onNextObject(InputKineticStream in, Object obj, Class<?> fieldType)
            throws Exception {
        var rtpsStream = (RtpsInputKineticStream) in;
        if (fieldType == Header.class) {
            // reading it manually to perform validation
            return new Result(true, rtpsStream.readHeader());
        }
        if (fieldType == SequenceNumber.class) {
            // reading it manually in custom format
            return new Result(true, rtpsStream.readSequenceNumber());
        }
        if (fieldType == SequenceNumberSet.class) {
            // reading it manually in custom format
            return new Result(true, rtpsStream.readSequenceNumberSet());
        }
        if (fieldType == EntityId.class) {
            // reading it manually in custom format
            return new Result(true, rtpsStream.readEntityId());
        }
        return Result.CONTINUE;
    }
}
