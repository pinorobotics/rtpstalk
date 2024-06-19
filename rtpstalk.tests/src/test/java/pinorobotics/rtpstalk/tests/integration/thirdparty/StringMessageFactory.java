/*
 * Copyright 2024 rtpstalk project
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
package pinorobotics.rtpstalk.tests.integration.thirdparty;

import id.pubsubtests.data.Message;
import id.pubsubtests.data.MessageFactory;
import id.pubsubtests.data.RandomMessageGenerator;
import pinorobotics.rtpstalk.tests.integration.thirdparty.cyclonedds.CycloneDdsHelloWorldClient;
import pinorobotics.rtpstalk.tests.integration.thirdparty.fastdds.FastRtpsHelloWorldClient;

/**
 * Thirdparty HelloWorld clients ({@link CycloneDdsHelloWorldClient}, {@link
 * FastRtpsHelloWorldClient}) support only String messages
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
class StringMessageFactory implements MessageFactory {

    @Override
    public Message create(String body) {
        return new StringMessage(body.getBytes());
    }

    @Override
    public Message create(byte[] body) {
        return new StringMessage(body);
    }

    @Override
    public RandomMessageGenerator createGenerator(long seed, int messageSizeInBytes) {
        return new RandomMessageGenerator(this, seed, messageSizeInBytes) {
            @Override
            public void populateMessage(Message message) {
                super.populateMessage(message);
                StringMessage.convertToSingleString(message.getBody());
            }

            @Override
            public Message nextRandomMessage() {
                return StringMessageFactory.this.create(super.nextRandomMessage().getBody());
            }
        };
    }
}
