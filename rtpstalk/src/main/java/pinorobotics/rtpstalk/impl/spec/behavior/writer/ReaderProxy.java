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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.structure.history.CacheChange;
import pinorobotics.rtpstalk.impl.spec.structure.history.HistoryCache;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;

/**
 * Current implementation of ReaderProxy does not store {@link CacheChange} changes since they
 * already present in {@link HistoryCache}. Instead it relies on highestSeqNumSent to track the
 * changes for each reader separately.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ReaderProxy implements AutoCloseable {

    private Guid remoteReaderGuid;
    private List<Locator> unicastLocatorList;
    private Set<Long> requestedchangesForReader = ConcurrentHashMap.newKeySet();
    private RtpsMessageSender sender;

    @RtpsSpecReference(
            paragraph = "8.4.15.1",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "The highestSeqNumSent would record the highest value of the\n"
                            + "sequence number of any CacheChange sent to the ReaderProxy.")
    private long highestSeqNumSent;

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
     * Returns sorted list of changes for the {@link ReaderProxy} that have status REQUESTED. This
     * represents the set of changes that were requested by the RTPS Reader represented by the
     * {@link ReaderProxy} using an ACKNACK Message.
     */
    public List<Long> requestedChanges() {
        return requestedchangesForReader.stream().sorted().toList();
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

    /**
     * All sequence numbers up to the one prior to given sequence number are confirmed as received
     * by the reader.
     */
    public void ackedChanges(long seqNum) {
        highestSeqNumSent = seqNum;
    }

    public long getHighestSeqNumSent() {
        return highestSeqNumSent;
    }

    public RtpsMessageSender getSender() {
        return sender;
    }

    @Override
    public void close() {
        sender.close();
    }
}
