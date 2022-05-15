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
package pinorobotics.rtpstalk.behavior.reader;

import id.xfunction.util.IntBitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.Guid;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.messages.ReliabilityKind;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.messages.walk.Result;
import pinorobotics.rtpstalk.structure.history.CacheChange;

/**
 * Reliable Statefull RTPS reader.
 *
 * <p>This should be thread safe since it is possible that one thread will be calling {@link
 * #matchedWriterAdd(Guid, List)} when another doing {@link #process(RtpsMessage)} and that
 * happening at the same time.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class StatefullRtpsReader<D extends Payload> extends RtpsReader<D> {

    /** Used to maintain state on the remote Writers matched up with the Reader. */
    private Map<Guid, WriterInfo> matchedWriters = new ConcurrentHashMap<>();

    private RtpsTalkConfiguration config;
    private OperatingEntities operatingEntities;

    public StatefullRtpsReader(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            OperatingEntities operatingEntities,
            EntityId entityId) {
        super(
                config,
                tracingToken,
                new Guid(config.guidPrefix(), entityId),
                ReliabilityKind.RELIABLE);
        this.config = config;
        this.operatingEntities = operatingEntities;
        operatingEntities.add(getGuid().entityId, this);
    }

    public void matchedWriterAdd(Guid remoteGuid, List<Locator> unicast) {
        var proxy = new WriterProxy(getGuid(), remoteGuid, unicast);
        logger.fine("Adding writer proxy for writer with guid {0}", proxy.getRemoteWriterGuid());
        matchedWriters.put(
                proxy.getRemoteWriterGuid(),
                new WriterInfo(
                        proxy, new WriterHeartbeatProcessor(getTracingToken(), config, proxy)));
    }

    public void matchedWriterRemove(Guid writer) {
        if (matchedWriters.remove(writer) == null) {
            logger.warning("Trying to remove unknwon matched writer {0}, ignoring...", writer);
        } else {
            logger.warning("Matched writer {0} is removed", writer);
        }
    }

    @Override
    public Result onHeartbeat(GuidPrefix guidPrefix, Heartbeat heartbeat) {
        // However, if the FinalFlag is not set, then the Reader must send an AckNack
        // message (8.3.7.5.5)
        if (!heartbeat.isFinal()) {
            var writerGuid = new Guid(guidPrefix, heartbeat.writerId);
            var writerInfo = matchedWriters.get(writerGuid);
            if (writerInfo != null) {
                logger.fine("Received heartbeat from writer {0}", writerGuid);
                writerInfo.heartbeatProcessor().addHeartbeat(heartbeat);
            } else {
                logger.fine("Received heartbeat from unknown writer {0}, ignoring...", writerGuid);
            }
        }
        return super.onHeartbeat(guidPrefix, heartbeat);
    }

    @Override
    public Result onAckNack(GuidPrefix guidPrefix, AckNack ackNack) {
        var readerProxyOpt =
                operatingEntities
                        .findStatefullWriter(ackNack.writerId)
                        .flatMap(
                                writer ->
                                        writer.matchedReaderLookup(
                                                new Guid(guidPrefix, ackNack.readerId)));
        if (readerProxyOpt.isEmpty()) {
            logger.fine(
                    "Received AckNack for unknown writer {0}, ignoring it...", ackNack.writerId);
        } else {
            var readerProxy = readerProxyOpt.get();
            var set = ackNack.readerSNState;
            var base = set.bitmapBase.value;
            var bitset = new IntBitSet(set.bitmap);
            readerProxy.requestedChangesClear();
            for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
                readerProxy.requestChange(base + i);
            }
        }
        return super.onAckNack(guidPrefix, ackNack);
    }

    @Override
    protected boolean addChange(CacheChange<D> cacheChange) {
        var isAdded = super.addChange(cacheChange);
        if (!isAdded) return false;
        var writerInfo = matchedWriters.get(cacheChange.getWriterGuid());
        if (writerInfo != null) {
            writerInfo.proxy().receivedChangeSet(cacheChange.getSequenceNumber());
        } else {
            logger.fine(
                    "No matched writer with guid {0} found for a new change, ignoring...",
                    cacheChange.getWriterGuid());
        }
        return true;
    }

    @Override
    protected void process(RtpsMessage message) {
        super.process(message);
        matchedWriters.values().stream()
                .map(WriterInfo::heartbeatProcessor)
                .forEach(WriterHeartbeatProcessor::ack);
    }

    protected OperatingEntities getOperatingEntities() {
        return operatingEntities;
    }
}
