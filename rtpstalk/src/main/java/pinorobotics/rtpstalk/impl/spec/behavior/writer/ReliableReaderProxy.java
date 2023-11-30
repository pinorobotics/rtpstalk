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
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ReliableReaderProxy implements ReaderProxy {

    private Guid remoteReaderGuid;
    private List<Locator> unicastLocatorList;
    private Set<Long> requestedchangesForReader = ConcurrentHashMap.newKeySet();
    private RtpsMessageSender sender;
    private ReaderQosPolicySet qosPolicy;

    @RtpsSpecReference(
            paragraph = "8.4.15.1",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "The highestSeqNumSent would record the highest value of the\n"
                            + "sequence number of any CacheChange sent to the ReaderProxy.")
    private long highestSeqNumSent;

    public ReliableReaderProxy(
            Guid remoteReaderGuid,
            List<Locator> unicastLocatorList,
            RtpsMessageSender sender,
            ReaderQosPolicySet qosPolicy) {
        this.remoteReaderGuid = remoteReaderGuid;
        this.unicastLocatorList = unicastLocatorList;
        this.sender = sender;
        this.qosPolicy = qosPolicy;
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
        return requestedchangesForReader.stream().sorted().toList();
    }

    @Override
    public void requestedChangesClear() {
        requestedchangesForReader.clear();
    }

    @Override
    public void requestChange(long seqNum) {
        requestedchangesForReader.add(seqNum);
    }

    @Override
    public void ackedChanges(long seqNum) {
        highestSeqNumSent = Math.max(highestSeqNumSent, seqNum);
    }

    @Override
    public long getHighestAckedSeqNum() {
        return highestSeqNumSent;
    }

    @Override
    public RtpsMessageSender getSender() {
        return sender;
    }

    @Override
    public ReaderQosPolicySet getQosPolicy() {
        return qosPolicy;
    }
}
