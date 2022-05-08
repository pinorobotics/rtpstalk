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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.TracingToken;
import pinorobotics.rtpstalk.messages.Locator;
import pinorobotics.rtpstalk.transport.DataChannel;
import pinorobotics.rtpstalk.transport.DataChannelFactory;

/** @author lambdaprime intid@protonmail.com */
public class TestDataChannelFactory extends DataChannelFactory {

    private RtpsTalkConfiguration config;
    private Map<Locator, TestDataChannel> channels = new HashMap<>();

    public TestDataChannelFactory(RtpsTalkConfiguration config) {
        super(config);
        this.config = config;
    }

    @Override
    public DataChannel connect(TracingToken token, Locator locator) throws IOException {
        return getOrCreateDataChannel(locator);
    }

    @Override
    public DataChannel bind(TracingToken token, Locator locator) throws IOException {
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
