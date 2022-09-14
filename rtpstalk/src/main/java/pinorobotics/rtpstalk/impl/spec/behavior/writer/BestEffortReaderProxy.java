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
package pinorobotics.rtpstalk.impl.spec.behavior.writer;

import java.util.List;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class BestEffortReaderProxy implements ReaderProxy {

    private Guid remoteReaderGuid;
    private List<Locator> unicastLocatorList;
    private RtpsMessageSender sender;

    public BestEffortReaderProxy(
            Guid remoteReaderGuid, List<Locator> unicastLocatorList, RtpsMessageSender sender) {
        this.remoteReaderGuid = remoteReaderGuid;
        this.unicastLocatorList = unicastLocatorList;
        this.sender = sender;
    }

    @Override
    public Guid getRemoteReaderGuid() {
        return remoteReaderGuid;
    }

    @Override
    public List<Locator> getUnicastLocatorList() {
        return unicastLocatorList;
    }

    @Override
    public List<Long> requestedChanges() {
        return List.of();
    }

    @Override
    public void requestedChangesClear() {}

    @Override
    public void requestChange(long seqNum) {}

    @Override
    public void ackedChanges(long seqNum) {}

    @Override
    public long getHighestSeqNumSent() {
        return Long.MAX_VALUE;
    }

    @Override
    public RtpsMessageSender getSender() {
        return sender;
    }

    @Override
    public ReliabilityQosPolicy.Kind getReliabilityKind() {
        return ReliabilityQosPolicy.Kind.BEST_EFFORT;
    }
}
