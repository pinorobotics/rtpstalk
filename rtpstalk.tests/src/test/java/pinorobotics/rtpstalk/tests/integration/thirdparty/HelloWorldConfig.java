/*
 * Copyright 2024 pinorobotics
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

import java.util.Map;
import java.util.Objects;

public record HelloWorldConfig(Map<HelloWorldExampleVariable, String> parameters) {
    public static final String DEFAULT_TOPIC_NAME = "HelloWorldTopic";
    public static final String DEFAULT_TOPIC_TYPE = "HelloWorld";

    public HelloWorldConfig() {
        this(Map.of());
    }

    public boolean isPublisher() {
        return Objects.equals(
                parameters.getOrDefault(HelloWorldExampleVariable.RunPublisher, "false"), "true");
    }

    public boolean isSubscriber() {
        return Objects.equals(
                parameters.getOrDefault(HelloWorldExampleVariable.RunSubscriber, "false"), "true");
    }
}
