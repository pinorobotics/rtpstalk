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
package pinorobotics.rtpstalk.tests.spec.discovery.spdp;

import id.xfunction.logging.TracingToken;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannel;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannelFactory;
import pinorobotics.rtpstalk.tests.TestConstants;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class TestDataChannelFactory extends DataChannelFactory {

    private RtpsTalkConfiguration config;
    private Map<Locator, TestDataChannel> channels = new HashMap<>();

    public TestDataChannelFactory() {
        this(TestConstants.TEST_CONFIG);
    }

    public TestDataChannelFactory(RtpsTalkConfiguration config) {
        super(config);
        this.config = config;
    }

    @Override
    public DataChannel connect(TracingToken token, List<Locator> locators) throws IOException {
        return getOrCreateDataChannel(locators.get(0));
    }

    @Override
    public DataChannel bindMulticast(
            TracingToken tracingToken, NetworkInterface networkInterface, Locator locator)
            throws IOException {
        return getOrCreateDataChannel(locator);
    }

    public void addChannel(Locator locator, TestDataChannel channel) {
        channels.put(locator, channel);
    }

    private DataChannel getOrCreateDataChannel(Locator locator) {
        var channel = channels.get(locator);
        if (channel == null) {
            channel = new TestDataChannel(config.guidPrefix(), false);
            channels.put(locator, channel);
        }
        return channel;
    }

    public Map<Locator, TestDataChannel> getChannels() {
        return Collections.unmodifiableMap(channels);
    }
}
