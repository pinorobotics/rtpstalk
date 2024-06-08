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
package pinorobotics.rtpstalk.tests.integration.thirdparty;

import id.xfunction.lang.XProcess;
import java.util.Collection;
import java.util.Map;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public interface HelloWorldExample extends AutoCloseable {

    Collection<RtpsTalkDataMessage> generateMessages(int numberOfMessages);

    XProcess runHelloWorldExample(Map<HelloWorldExampleVariable, String> config);

    String generateExpectedPublisherStdout(int numberOfMessages, String topicName);

    String generateExpectedSubscriberStdout(int numberOfMessages, String topic);

    @Override
    void close();
}
