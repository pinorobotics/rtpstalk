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
package pinorobotics.rtpstalk.tests.spec.transport;

import id.xfunction.logging.TracingToken;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.impl.spec.messages.RtpsMessage;
import pinorobotics.rtpstalk.impl.spec.transport.RtpsMessageReceiver;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class TestRtpsMessageReceiver extends RtpsMessageReceiver {

    private int subscribeCount;

    public TestRtpsMessageReceiver(TracingToken token) {
        super(TestConstants.TEST_CONFIG, token, TestConstants.TEST_PUBLISHER_EXECUTOR);
    }

    @Override
    public void subscribe(Subscriber<? super RtpsMessage> subscriber) {
        super.subscribe(subscriber);
        subscribeCount++;
    }

    public int getSubscribeCount() {
        return subscribeCount;
    }
}
