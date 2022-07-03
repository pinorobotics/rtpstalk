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
package pinorobotics.rtpstalk.impl;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.impl.spec.behavior.OperatingEntities;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.transport.DataChannel;

/**
 * Placeholder for all RTPS endpoints which are available on a certain network interface.
 *
 * @author lambdaprime intid@protonmail.com
 */
public class RtpsNetworkInterface {

    private Locator defaultUnicastLocator;
    private Locator metatrafficUnicastLocator;
    private OperatingEntities operatingEntities;
    private DataChannel defaultUnicastChannel;
    private DataChannel metatrafficUnicastChannel;

    public RtpsNetworkInterface(
            TracingToken tracingToken,
            DataChannel defaultUnicastChannel,
            Locator defaultUnicastLocator,
            DataChannel metatrafficUnicastChannel,
            Locator metatrafficUnicastLocator) {
        this.defaultUnicastChannel = defaultUnicastChannel;
        this.defaultUnicastLocator = defaultUnicastLocator;
        this.metatrafficUnicastChannel = metatrafficUnicastChannel;
        this.metatrafficUnicastLocator = metatrafficUnicastLocator;
        operatingEntities = new OperatingEntities(tracingToken);
    }

    public Locator getLocalDefaultUnicastLocator() {
        return defaultUnicastLocator;
    }

    public Locator getLocalMetatrafficUnicastLocator() {
        return metatrafficUnicastLocator;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("metatrafficUnicastLocator", metatrafficUnicastLocator);
        builder.append("defaultUnicastLocator", defaultUnicastLocator);
        return builder.toString();
    }

    public OperatingEntities getOperatingEntities() {
        return operatingEntities;
    }

    public DataChannel getDefaultUnicastChannel() {
        return defaultUnicastChannel;
    }

    public DataChannel getMetatrafficUnicastChannel() {
        return metatrafficUnicastChannel;
    }
}
