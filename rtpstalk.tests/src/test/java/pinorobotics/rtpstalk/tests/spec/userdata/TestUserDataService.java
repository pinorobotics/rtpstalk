/*
 * Copyright 2023 rtpstalk project
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

import id.xfunction.concurrent.SameThreadExecutorService;
import pinorobotics.rtpstalk.impl.PublisherDetails;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.userdata.UserDataService;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.spec.discovery.spdp.TestDataChannelFactory;
import pinorobotics.rtpstalk.tests.spec.transport.TestRtpsMessageReceiverFactory;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class TestUserDataService extends UserDataService {

    public TestUserDataService() {
        super(
                TestConstants.TEST_CONFIG_INTERNAL,
                new SameThreadExecutorService(),
                new TestDataChannelFactory(TestConstants.TEST_CONFIG),
                new TestDataObjectsFactory(true),
                new TestRtpsMessageReceiverFactory());
    }

    @Override
    public void publish(
            EntityId writerEntityId, EntityId readerEntityId, PublisherDetails publisherDetails) {}
}