/*
 * Copyright 2024 rtpstalk project
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
package pinorobotics.rtpstalk.impl.behavior.reader;

import java.util.Optional;
import pinorobotics.rtpstalk.impl.messages.ProtocolParameterMap;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.KeyHash;
import pinorobotics.rtpstalk.impl.spec.messages.StatusInfo;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ReaderUtils {
    public Optional<Guid> findDisposedObject(ProtocolParameterMap params) {
        var isDisposed =
                params.getFirstParameter(ParameterId.PID_STATUS_INFO, StatusInfo.class)
                        .filter(StatusInfo::isDisposed)
                        .isPresent();
        if (!isDisposed) return Optional.empty();
        return params.getFirstParameter(ParameterId.PID_KEY_HASH, KeyHash.class)
                .map(KeyHash::asGuid);
    }
}
