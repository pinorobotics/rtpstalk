/*
 * Copyright 2022 pinorobotics
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
package pinorobotics.rtpstalk.impl.spec;

import id.xfunction.util.ImmutableMultiMap;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.StatusInfo;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class DataFactory {

    public ParameterList createReaderDisposedSubscriptionData(Guid readerGuid) {
        var info = new StatusInfo(StatusInfo.Flags.DISPOSED);
        var pl =
                ParameterList.ofProtocolParameters(
                        ImmutableMultiMap.of(
                                ParameterId.PID_STATUS_INFO, info,
                                ParameterId.PID_KEY_HASH, readerGuid));
        return pl;
    }
}
