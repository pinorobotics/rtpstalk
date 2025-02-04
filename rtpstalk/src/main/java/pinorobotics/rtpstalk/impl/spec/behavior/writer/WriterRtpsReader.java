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

import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.logging.TracingToken;
import id.xfunction.logging.XLogger;
import id.xfunction.util.IntBitSet;
import java.util.ArrayList;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.AckNack;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.impl.spec.messages.walk.Result;
import pinorobotics.rtpstalk.impl.spec.messages.walk.RtpsSubmessageVisitor;
import pinorobotics.rtpstalk.impl.spec.messages.walk.RtpsSubmessagesWalker;
import pinorobotics.rtpstalk.messages.RtpsTalkMessage;

/**
 * Serves mainly for heartbeat purposes of local writer and process ackNacks from remote readers.
 *
 * @author lambdaprime intid@protonmail.com
 */
public class WriterRtpsReader<D extends RtpsTalkMessage> extends SimpleSubscriber<RtpsMessage>
        implements RtpsSubmessageVisitor {

    private final XLogger logger;
    private RtpsSubmessagesWalker walker = new RtpsSubmessagesWalker();
    private StatefullReliableRtpsWriter<D> writer;

    public WriterRtpsReader(TracingToken tracingToken, StatefullReliableRtpsWriter<D> writer) {
        this.writer = writer;
        logger = XLogger.getLogger(getClass(), tracingToken);
    }

    @Override
    public Result onAckNack(GuidPrefix guidPrefix, AckNack ackNack) {
        var readerGuid = new Guid(guidPrefix, ackNack.readerId);
        var readerProxy = writer.matchedReaderLookup(readerGuid).orElse(null);
        if (readerProxy == null) {
            logger.fine(
                    "No matched reader {0} for writer {1}, ignoring it...",
                    readerGuid, ackNack.writerId);
            return Result.CONTINUE;
        }
        logger.fine(
                "Processing acknack for writer {0} received from reader {1}",
                ackNack.writerId, readerGuid);
        var set = ackNack.readerSNState;
        var base = set.bitmapBase.value;
        var bitset = new IntBitSet(set.bitmap);
        if (readerProxy.ackedChanges(set.bitmapBase.value - 1) > 0) writer.cleanupCacheAndRequest();
        var requested = new ArrayList<Long>();
        for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
            requested.add(base + i);
        }
        readerProxy.requestedChanges(requested);
        return Result.CONTINUE;
    }

    @Override
    public void onNext(RtpsMessage message) {
        try {
            walker.walk(message, this);
        } catch (Exception e) {
            logger.severe(e);
        } finally {
            subscription.request(1);
        }
    }
}
