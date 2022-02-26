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
package pinorobotics.rtpstalk.behavior.writer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.transport.RtpsMessageSender;

public class ReaderProxy {

    private Guid remoteReaderGuid;
    private List<Locator> unicastLocatorList;
    private Set<Long> requestedchangesForReader = new LinkedHashSet<>();
    private RtpsMessageSender sender;

    public ReaderProxy(
            Guid remoteReaderGuid, List<Locator> unicastLocatorList, RtpsMessageSender sender) {
        this.remoteReaderGuid = remoteReaderGuid;
        this.unicastLocatorList = unicastLocatorList;
        this.sender = sender;
    }

    /** Identifies the remote matched RTPS Reader that is represented by the ReaderProxy. */
    public Guid getRemoteReaderGuid() {
        return remoteReaderGuid;
    }

    /**
     * List of unicast (address, port) combinations that can be used to send messages to the matched
     * Writer or Writers. The list may be empty.
     */
    public List<Locator> getUnicastLocatorList() {
        return unicastLocatorList;
    }

    /**
     * Returns the subset of changes for the {@link ReaderProxy} that have status REQUESTED. This
     * represents the set of changes that were requested by the RTPS Reader represented by the
     * {@link ReaderProxy} using an ACKNACK Message.
     */
    public Set<Long> requestedChanges() {
        return requestedchangesForReader;
    }

    /** */
    public void requestedChangesClear() {
        requestedchangesForReader.clear();
    }

    /**
     * This operation modifies the ChangeForReader status of a set of changes for the RTPS Reader
     * represented by this {@link ReaderProxy}. The change with given sequence number has its status
     * changed to REQUESTED.
     */
    public void requestChange(long seqNum) {
        requestedchangesForReader.add(seqNum);
    }

    public RtpsMessageSender getSender() {
        return sender;
    }
}
