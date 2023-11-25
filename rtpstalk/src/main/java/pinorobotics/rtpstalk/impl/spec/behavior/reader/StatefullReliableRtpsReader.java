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

import id.xfunction.logging.TracingToken;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.behavior.reader.WriterHeartbeatProcessor;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
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
import pinorobotics.rtpstalk.impl.spec.structure.history.HistoryCache.AddResult;
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

    private RtpsTalkConfiguration config;
    private LocalOperatingEntities operatingEntities;

    public StatefullReliableRtpsReader(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            Class<D> messageType,
            Executor publisherExecutor,
            LocalOperatingEntities operatingEntities,
            EntityId entityId) {
        super(
                config,
                tracingToken,
                messageType,
                publisherExecutor,
                new Guid(config.guidPrefix(), entityId),
                ReliabilityQosPolicy.Kind.RELIABLE);
        this.config = config;
        this.operatingEntities = operatingEntities;
        operatingEntities.getLocalReaders().add(this);
    }

    public void matchedWriterAdd(Guid remoteGuid, List<Locator> unicast) {
        var proxy = new WriterProxy(getTracingToken(), config, getGuid(), remoteGuid, unicast);
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

    @Override
    protected AddResult addChange(CacheChange<D> cacheChange) {
        var result = super.addChange(cacheChange);
        if (result == AddResult.NOT_ADDED) return result;
        var writerProxy = matchedWriters.get(cacheChange.getWriterGuid());
        if (writerProxy != null) {
            writerProxy.receivedChangeSet(cacheChange.getSequenceNumber());
        } else {
            logger.fine(
                    "No matched writer with guid {0} found for a new change, ignoring...",
                    cacheChange.getWriterGuid());
        }
        return result;
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
