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

import id.xfunction.logging.XLogger;
import id.xfunction.util.IntBitSet;
import java.io.IOException;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.Header;
import pinorobotics.rtpstalk.messages.ProtocolId;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.AckNack;
import pinorobotics.rtpstalk.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.messages.submessages.InfoDestination;
import pinorobotics.rtpstalk.messages.submessages.Submessage;
import pinorobotics.rtpstalk.messages.submessages.elements.Count;
import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.messages.submessages.elements.VendorId;
import pinorobotics.rtpstalk.transport.DataChannel;
import pinorobotics.rtpstalk.transport.DataChannelFactory;

/**
 * Combines multiple heartbeats into one AckNack
 *
 * <p>8.4.15.2 Efficient use of Gap and AckNack Submessages
 */
/** @author aeon_flux aeon_flux@eclipso.ch */
public class WriterHeartbeatProcessor {

    private XLogger logger;
    private DataChannel dataChannel;
    private DataChannelFactory dataChannelFactory;
    private WriterProxy writerProxy;
    private int writerCount;
    private int count;
    private Heartbeat lastHeartbeat;
    private TracingToken tracingToken;

    public WriterHeartbeatProcessor(
            TracingToken tracingToken, RtpsTalkConfiguration config, WriterProxy writerProxy) {
        this.tracingToken = tracingToken;
        this.writerProxy = writerProxy;
        dataChannelFactory = new DataChannelFactory(config);
        logger = InternalUtils.getInstance().getLogger(getClass(), tracingToken);
    }

    /** Called when new Heartbeat received */
    public void addHeartbeat(Heartbeat heartbeat) {
        // However, if the FinalFlag is not set, then the Reader must send an AckNack
        // message (8.3.7.5.5)
        if (heartbeat.isFinal()) {
            logger.fine("Received final heartbeat, ignoring...");
            return;
        }
        if (writerCount < heartbeat.count.value) {
            writerCount = heartbeat.count.value;
            lastHeartbeat = heartbeat;
        } else {
            logger.fine("Received duplicate heartbeat, ignoring...");
        }
    }

    /** Ack all received heartbeats */
    public void ack() {
        if (lastHeartbeat == null) {
            logger.fine("No new heartbeats, nothing to acknowledge...");
            return;
        }

        var writerGuid = writerProxy.getRemoteWriterGuid();
        var readerGuid = writerProxy.getReaderGuid();

        logger.fine("Sending heartbeat ack for writer {0}", writerGuid);
        if (dataChannel == null) {
            var locator = writerProxy.getUnicastLocatorList().get(0);
            try {
                dataChannel = dataChannelFactory.connect(tracingToken, locator);
            } catch (IOException e) {
                logger.warning(
                        "Cannot open connection to remote writer on {0}: {1}",
                        locator, e.getMessage());
                return;
            }
        }

        var infoDst = new InfoDestination(writerGuid.guidPrefix);

        writerProxy.missingChangesUpdate(lastHeartbeat.lastSN.value);
        writerProxy.lostChangesUpdate(lastHeartbeat.firstSN.value);

        var ack =
                new AckNack(
                        readerGuid.entityId,
                        writerGuid.entityId,
                        createSequenceNumberSet(lastHeartbeat),
                        new Count(count++));
        var submessages = new Submessage[] {infoDst, ack};
        Header header =
                new Header(
                        ProtocolId.Predefined.RTPS.getValue(),
                        ProtocolVersion.Predefined.Version_2_3.getValue(),
                        VendorId.Predefined.RTPSTALK.getValue(),
                        readerGuid.guidPrefix);
        var message = new RtpsMessage(header, submessages);
        dataChannel.send(writerGuid, message);
        lastHeartbeat = null;
    }

    private SequenceNumberSet createSequenceNumberSet(Heartbeat heartbeat) {
        var missing = writerProxy.missingChanges();
        if (missing.length == 0) {
            return expectNextSet();
        }

        var first = heartbeat.firstSN.value;
        var last = heartbeat.lastSN.value;

        // enumeration from 1
        var numBits = (int) (last - first + 1);

        // Creates bitmask of missing changes between [first..last]
        var bset = new IntBitSet(numBits);
        for (var sn : missing) {
            if (sn < first || last < sn) continue;
            bset.flip((int) (sn - first));
        }
        return new SequenceNumberSet(lastHeartbeat.firstSN, numBits, bset.intArray());
    }

    private SequenceNumberSet expectNextSet() {
        // all present so we expect next
        return new SequenceNumberSet(new SequenceNumber(writerProxy.availableChangesMax() + 1), 0);
    }
}
