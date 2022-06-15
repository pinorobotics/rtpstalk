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
package pinorobotics.rtpstalk.tests.spec.transport;

import java.util.ArrayList;
import java.util.List;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiver;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiverFactory;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class TestRtpsMessageReceiverFactory extends RtpsMessageReceiverFactory {
    private List<TestRtpsMessageReceiver> receivers = new ArrayList<>();

    @Override
    public RtpsMessageReceiver newRtpsMessageReceiver(TracingToken token) {
        var receiver = new TestRtpsMessageReceiver(token);
        receivers.add(receiver);
        return receiver;
    }

    public List<TestRtpsMessageReceiver> getReceivers() {
        return receivers;
    }
}
