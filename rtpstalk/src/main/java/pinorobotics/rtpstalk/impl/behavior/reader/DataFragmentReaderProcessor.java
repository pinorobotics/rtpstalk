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
package pinorobotics.rtpstalk.impl.behavior.reader;

import id.xfunction.logging.TracingToken;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class DataFragmentReaderProcessor {

    private record DataKey(Guid writerGuid, long dataSequenceNumber) {}

    private Map<DataKey, DataFragmentJoiner> joiners = new HashMap<>();
    private TracingToken tracingToken;

    public DataFragmentReaderProcessor(TracingToken token) {
        this.tracingToken = token;
    }

    public Optional<RtpsTalkDataMessage> addDataFrag(Guid writerGuid, DataFrag dataFrag) {
        var dataKey = new DataKey(writerGuid, dataFrag.writerSN.value);
        var joiner =
                joiners.computeIfAbsent(
                        dataKey, i -> new DataFragmentJoiner(tracingToken, dataFrag));
        joiner.add(dataFrag);
        return joiner.hasAllFragments() ? Optional.of(joiner.join()) : Optional.empty();
    }
}
