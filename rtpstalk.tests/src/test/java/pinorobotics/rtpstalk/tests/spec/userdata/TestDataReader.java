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
package pinorobotics.rtpstalk.tests.spec.userdata;

import id.xfunction.logging.TracingToken;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.userdata.ReliableDataReader;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class TestDataReader extends ReliableDataReader {

    private int subscribeCount;

    public TestDataReader(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            LocalOperatingEntities operatingEntities,
            EntityId entityId,
            Executor executor,
            int maxBufferCapacity) {
        super(
                config,
                tracingToken,
                executor,
                operatingEntities,
                entityId,
                new ReaderQosPolicySet(),
                TestConstants.TEST_DATA_CHANNEL_FACTORY);
    }

    @Override
    public void subscribe(Subscriber<? super RtpsTalkDataMessage> subscriber) {
        super.subscribe(subscriber);
        subscribeCount++;
    }

    public int getSubscribeCount() {
        return subscribeCount;
    }
}
