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

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import pinorobotics.rtpstalk.messages.RtpsMessage;
import pinorobotics.rtpstalk.messages.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.transport.DataChannel;

/** @author lambdaprime intid@protonmail.com */
public class TestDataChannel extends DataChannel {

    protected TestDataChannel(GuidPrefix prefix) {
        super(null, null, prefix, 0);
    }

    private BlockingQueue<RtpsMessage> input = new LinkedBlockingQueue<>();
    private List<RtpsMessage> output = new CopyOnWriteArrayList<>();

    public TestDataChannel withInput(List<RtpsMessage> input) {
        this.input.addAll(input);
        return this;
    }

    @Override
    public RtpsMessage receive() throws Exception {
        return input.take();
    }

    @Override
    public void send(RtpsMessage message) {
        output.add(message);
    }

    /** List of RTPS messages sent to remote participant */
    public List<RtpsMessage> getOutput() {
        return output;
    }
}
