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
package pinorobotics.rtpstalk.tests.discovery.spdp;

import id.xfunction.function.Unchecked;
import java.net.StandardProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.transport.DataChannel;

/** @author lambdaprime intid@protonmail.com */
public class TestDataChannel extends DataChannel {

    private BlockingQueue<RtpsMessage> dataQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<RtpsMessage> inputQueue = new LinkedBlockingQueue<>();
    private boolean blockReceiveForever;

    protected TestDataChannel(GuidPrefix prefix, boolean blockReceiveForever) {
        super(
                new TracingToken("test"),
                Unchecked.get(() -> DatagramChannel.open(StandardProtocolFamily.INET)),
                null,
                prefix,
                0);
        this.blockReceiveForever = blockReceiveForever;
    }

    public TestDataChannel withInput(List<RtpsMessage> input) {
        inputQueue.addAll(input);
        return this;
    }

    public void addInput(RtpsMessage... inputs) {
        for (var in : inputs) inputQueue.add(in);
    }

    @Override
    public RtpsMessage receive() throws Exception {
        if (blockReceiveForever) {
            new CompletableFuture<Void>().get();
        }
        if (inputQueue != null) {
            return inputQueue.take();
        }
        return dataQueue.take();
    }

    @Override
    public void send(RtpsMessage message) {
        dataQueue.add(message);
    }

    public BlockingQueue<RtpsMessage> getDataQueue() {
        return dataQueue;
    }
}
