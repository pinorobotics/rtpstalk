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
package pinorobotics.rtpstalk.impl.spec.transport.io;

import id.kineticstreamer.InputKineticStream;
import id.kineticstreamer.KineticStreamController;
import id.kineticstreamer.OutputKineticStream;
import pinorobotics.rtpstalk.impl.spec.messages.Header;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
class RtpsKineticStreamController extends KineticStreamController {

    @Override
    public ReaderResult onNextObject(InputKineticStream in, Object obj, Class<?> fieldType)
            throws Exception {
        var rtpsStream = (RtpsInputKineticStream) in;
        if (fieldType == Header.class) {
            // reading it manually to perform validation
            return new ReaderResult(true, rtpsStream.readHeader());
        }
        if (fieldType == SequenceNumber.class) {
            // reading it manually in custom format
            return new ReaderResult(true, rtpsStream.readSequenceNumber());
        }
        if (fieldType == SequenceNumberSet.class) {
            // reading it manually in custom format
            return new ReaderResult(true, rtpsStream.readSequenceNumberSet());
        }
        if (fieldType == EntityId.class) {
            // reading it manually in custom format
            return new ReaderResult(true, rtpsStream.readEntityId());
        }
        return ReaderResult.CONTINUE;
    }

    @Override
    public WriterResult onNextObject(OutputKineticStream in, Object obj) throws Exception {
        var rtpsStream = (RtpsOutputKineticStream) in;
        if (obj instanceof ParameterList pl) {
            // writing it manually since we convert it to custom type
            rtpsStream.writeParameterList(pl);
            return new WriterResult(true);
        }
        if (obj instanceof SequenceNumber num) {
            // writing it manually in custom format
            rtpsStream.writeSequenceNumber(num);
            return new WriterResult(true);
        }
        if (obj instanceof SequenceNumberSet set) {
            // writing it manually in custom format
            rtpsStream.writeSequenceNumberSet(set);
            return new WriterResult(true);
        }
        if (obj instanceof EntityId entiyId) {
            // writing it manually in custom format
            rtpsStream.writeEntityId(entiyId);
            return new WriterResult(true);
        }
        return WriterResult.CONTINUE;
    }
}
