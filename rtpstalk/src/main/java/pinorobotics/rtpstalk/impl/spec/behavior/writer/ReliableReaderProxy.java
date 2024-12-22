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
package pinorobotics.rtpstalk.impl.spec.behavior.writer;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;

/**
 * Must be Thread-safe because it is shared between Writer background scheduler thread as well as
 * Executor which delivers messages from the remote Readers ({@link WriterRtpsReader}). In
 * particular, when {@link WriterRtpsReader} processes Ack requests and executes {@link
 * #requestedChanges(Collection)} the Writer may be in the middle of sending {@link
 * #requestedChanges()}
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ReliableReaderProxy implements ReaderProxy {

    private final Guid remoteReaderGuid;
    private final List<Locator> unicastLocatorList;
    private final RtpsMessageSender sender;
    private final ReaderQosPolicySet qosPolicy;
    private volatile List<Long> immutableListOfRequestedChanges = List.of();

    @RtpsSpecReference(
            paragraph = "8.4.15.1",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "The highestSeqNumSent would record the highest value of the\n"
                            + "sequence number of any CacheChange sent to the ReaderProxy.")
    private AtomicLong highestSeqNumSent = new AtomicLong();

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
        return immutableListOfRequestedChanges;
    }

    @Override
    public void requestedChanges(Collection<Long> requested) {
        immutableListOfRequestedChanges = requested.stream().sorted().distinct().toList();
    }

    @Override
    public long ackedChanges(long seqNum) {
        var diff =
                seqNum - highestSeqNumSent.getAndUpdate(highSeqNum -> Math.max(seqNum, highSeqNum));
        if (diff <= 0) return 0;
        immutableListOfRequestedChanges =
                immutableListOfRequestedChanges.stream().filter(sn -> sn >= seqNum).toList();
        return diff;
    }

    @Override
    public long getHighestAckedSeqNum() {
        return highestSeqNumSent.get();
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
