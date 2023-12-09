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
package pinorobotics.rtpstalk.impl.spec.behavior.reader;

import id.xfunction.Preconditions;
import id.xfunction.logging.TracingToken;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.behavior.reader.WriterHeartbeatProcessor;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.walk.Result;
import pinorobotics.rtpstalk.impl.spec.structure.history.CacheChange;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * Reliable Statefull RTPS reader.
 *
 * <p>This should be thread safe since it is possible that one thread will be calling {@link
 * #matchedWriterAdd(Guid, List)} when another doing {@link #process(RtpsMessage)} and that
 * happening at the same time.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class StatefullReliableRtpsReader<D extends RtpsTalkMessage> extends RtpsReader<D> {

    /** Used to maintain state on the remote Writers matched up with the Reader. */
    private Map<Guid, WriterProxy> matchedWriters = new ConcurrentHashMap<>();

    /** Not need to be thread-safe since {@link RtpsReader} requests one message at a time */
    private Map<Guid, Long> lastSubmittedSeqNum = new HashMap<>();

    private RtpsTalkConfigurationInternal config;
    private LocalOperatingEntities operatingEntities;
    private ReaderQosPolicySet qosPolicy;

    private DataChannelFactory dataChannelFactory;

    public StatefullReliableRtpsReader(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            Class<D> messageType,
            Executor publisherExecutor,
            LocalOperatingEntities operatingEntities,
            EntityId entityId,
            ReaderQosPolicySet qosPolicy) {
        this(
                config,
                tracingToken,
                messageType,
                publisherExecutor,
                operatingEntities,
                entityId,
                qosPolicy,
                new DataChannelFactory(config.publicConfig()));
    }

    public StatefullReliableRtpsReader(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            Class<D> messageType,
            Executor publisherExecutor,
            LocalOperatingEntities operatingEntities,
            EntityId entityId,
            ReaderQosPolicySet qosPolicy,
            DataChannelFactory dataChannelFactory) {
        super(
                config.publicConfig(),
                tracingToken,
                messageType,
                publisherExecutor,
                new Guid(config.publicConfig().guidPrefix(), entityId),
                ReliabilityQosPolicy.Kind.RELIABLE);
        this.dataChannelFactory = dataChannelFactory;
        Preconditions.equals(qosPolicy.reliabilityKind(), ReliabilityQosPolicy.Kind.RELIABLE);
        this.qosPolicy = qosPolicy;
        this.config = config;
        this.operatingEntities = operatingEntities;
        operatingEntities.getLocalReaders().add(this);
    }

    public void matchedWriterAdd(Guid remoteGuid, List<Locator> unicast) {
        var proxy =
                new WriterProxy(
                        getTracingToken(),
                        dataChannelFactory,
                        config.maxSubmessageSize(),
                        getGuid(),
                        remoteGuid,
                        unicast);
        logger.fine("Adding writer proxy for writer with guid {0}", proxy.getRemoteWriterGuid());
        matchedWriters.put(proxy.getRemoteWriterGuid(), proxy);
    }

    public void matchedWriterRemove(Guid writer) {
        if (matchedWriters.remove(writer) == null) {
            logger.warning("Trying to remove unknwon matched writer {0}, ignoring...", writer);
        } else {
            logger.warning("Matched writer {0} is removed", writer);
        }
    }

    public void matchedWritersRemove(GuidPrefix guidPrefix) {
        logger.fine("Removing all matched writers with guidPrefix {0}", guidPrefix);
        matchedWriters.keySet().stream()
                .filter(guid -> guid.guidPrefix.equals(guidPrefix))
                .forEach(this::matchedWriterRemove);
    }

    @RtpsSpecReference(
            paragraph = "8.3.7.5.5",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "However, if the FinalFlag is not set, then the Reader must send an AckNack"
                            + " message")
    @Override
    public Result onHeartbeat(GuidPrefix guidPrefix, Heartbeat heartbeat) {
        if (!heartbeat.isFinal()) {
            var writerGuid = new Guid(guidPrefix, heartbeat.writerId);
            var writerProxy = matchedWriters.get(writerGuid);
            if (writerProxy != null) {
                logger.fine("Received heartbeat from writer {0}", writerGuid);
                writerProxy.getHeartbeatProcessor().addHeartbeat(heartbeat);
            } else {
                logger.fine("Received heartbeat from unknown writer {0}, ignoring...", writerGuid);
            }
        }
        return super.onHeartbeat(guidPrefix, heartbeat);
    }

    @RtpsSpecReference(
            paragraph = "8.4.1.1.9.b",
            protocolVersion = Predefined.Version_2_3,
            text =
                    """
                    For a RELIABLE DDS DataReader, changes in its RTPS Reader’s HistoryCache are made visible to the user
                    application only when all previous changes (i.e., changes with smaller sequence numbers) are also visible.
                    """)
    @Override
    protected boolean addChange(CacheChange<D> newCacheChange) {
        logger.entering("addChange");
        Guid writerGuid = newCacheChange.getWriterGuid();
        var writerProxy = matchedWriters.get(writerGuid);
        if (writerProxy == null) {
            logger.fine(
                    "No matched writer with guid {0} found for a new change, ignoring...",
                    writerGuid);
            return false;
        }
        var cache = getReaderCache();
        var isAdded = cache.addChange(newCacheChange);
        if (isAdded) {
            var lastSeqNum = lastSubmittedSeqNum.get(writerGuid);
            if (lastSeqNum == null) {
                lastSeqNum = calcStartSeqNum(newCacheChange.getSequenceNumber());
                if (lastSeqNum == 0) {
                    writerProxy.missingChangesUpdate(newCacheChange.getSequenceNumber());
                }
            }
            writerProxy.receivedChangeSet(newCacheChange.getSequenceNumber());
            var iter = cache.getAllSortedBySeqNum(writerGuid, lastSeqNum).iterator();
            while (iter.hasNext()) {
                var change = iter.next();
                if (lastSeqNum + 1 != change.getSequenceNumber()) break;
                lastSeqNum = change.getSequenceNumber();
                submitChangeToUser(change);
            }
            lastSubmittedSeqNum.put(writerGuid, lastSeqNum);
        }
        logger.exiting("addChange");
        return isAdded;
    }

    private long calcStartSeqNum(long firstSeqNumReceived) {
        if (qosPolicy.durabilityKind() == DurabilityQosPolicy.Kind.TRANSIENT_LOCAL_DURABILITY_QOS)
            return 0;
        return firstSeqNumReceived - 1;
    }

    @Override
    protected void process(RtpsMessage message) {
        super.process(message);
        matchedWriters.values().stream()
                .map(WriterProxy::getHeartbeatProcessor)
                .forEach(WriterHeartbeatProcessor::ack);
    }

    protected LocalOperatingEntities getOperatingEntities() {
        return operatingEntities;
    }
}
