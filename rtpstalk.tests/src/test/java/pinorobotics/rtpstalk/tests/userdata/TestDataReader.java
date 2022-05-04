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
package pinorobotics.rtpstalk.tests.userdata;

import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscriber;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.submessages.RawData;
import pinorobotics.rtpstalk.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.userdata.DataReader;

/** @author aeon_flux aeon_flux@eclipso.ch */
public class TestDataReader extends DataReader {

    private int subscribeCount;

    public TestDataReader(
            RtpsTalkConfiguration config,
            TracingToken tracingToken,
            OperatingEntities operatingEntities,
            EntityId entityId,
            Executor executor,
            int maxBufferCapacity) {
        super(config, tracingToken, operatingEntities, entityId);
    }

    @Override
    public void subscribe(Subscriber<? super RawData> subscriber) {
        super.subscribe(subscriber);
        subscribeCount++;
    }

    public int getSubscribeCount() {
        return subscribeCount;
    }
}
