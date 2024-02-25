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
import java.util.function.Predicate;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.structure.history.CacheChange;
import pinorobotics.rtpstalk.impl.spec.structure.history.HistoryCache;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageSender;

/**
 * Current implementations of ReaderProxy does not store {@link CacheChange} changes since they
 * already present in {@link HistoryCache}. Instead it relies on highestSeqNumSent to track the
 * changes for each reader separately.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public interface ReaderProxy extends AutoCloseable {

    /** Identifies the remote matched RTPS Reader that is represented by the ReaderProxy. */
    Guid getRemoteReaderGuid();

    /**
     * List of unicast (address, port) combinations that can be used to send messages to the matched
     * Writer or Writers. The list may be empty.
     */
    List<Locator> getUnicastLocatorList();

    /**
     * Returns sorted list of changes for the {@link ReaderProxy} that have status REQUESTED. This
     * represents the set of changes that were requested by the RTPS Reader represented by the
     * {@link ReaderProxy} using an ACKNACK Message.
     */
    List<Long> requestedChanges();

    void requestedChangesClear();

    /**
     * This operation modifies the ChangeForReader status of a set of changes for the RTPS Reader
     * represented by this {@link ReaderProxy}. The change with given sequence number has its status
     * changed to REQUESTED.
     */
    void requestChange(long seqNum);

    /**
     * All sequence numbers up to the one prior to given sequence number are confirmed as received
     * by the reader.
     *
     * @return number of new changes which never been acked by the reader before
     */
    long ackedChanges(long seqNum);

    /** If ReaderProxy does not track such information it should return Long.MAX_VALUE */
    long getHighestAckedSeqNum();

    RtpsMessageSender getSender();

    ReaderQosPolicySet getQosPolicy();

    @Override
    default void close() {
        getSender().close();
    }

    static Predicate<ReaderProxy> IS_RELIABLE_FILTER =
            readerProxy ->
                    readerProxy.getQosPolicy().reliabilityKind()
                            == ReliabilityQosPolicy.Kind.RELIABLE;

    static Predicate<ReaderProxy> IS_BEST_ERRORT_FILTER =
            readerProxy ->
                    readerProxy.getQosPolicy().reliabilityKind()
                            == ReliabilityQosPolicy.Kind.BEST_EFFORT;
}
