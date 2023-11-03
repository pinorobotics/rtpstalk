/*
 * Copyright 2021 rtpstalk project
 *
 * Website: https://github.com/pinorobotics/rtpstalk
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * Java client for RTPS (Real-time Publish-Subscribe) protocol.
 *
 * <p>For usage examples see <a href="http://pinoweb.freetzi.com/rtpstalk">Documentation</a>
 *
 * @see <a
 *     href="https://github.com/pinorobotics/rtpstalk/blob/main/rtpstalk/release/CHANGELOG.md">Download</a>
 * @see <a href="https://github.com/pinorobotics/rtpstalk">Github</a>
 * @see <a href="http://pinoweb.freetzi.com/rtpstalk">Documentation</a>
 * @author aeon_flux aeon_flux@eclipso.ch
 * @author lambdaprime intid@protonmail.com
 */
module rtpstalk {
    requires id.xfunction;
    requires id.kineticstreamer;
    requires java.logging;
    requires io.opentelemetry.api;

    exports pinorobotics.rtpstalk;
    exports pinorobotics.rtpstalk.messages;
    exports pinorobotics.rtpstalk.qos;
    exports pinorobotics.rtpstalk.exceptions;
    exports pinorobotics.rtpstalk.impl to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.topics to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.qos to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.behavior.reader to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.behavior.writer to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.messages to
            id.kineticstreamer,
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.messages.walk to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.messages.submessages to
            id.kineticstreamer,
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.messages.submessages.elements to
            id.kineticstreamer,
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.transport to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.transport.io to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.discovery.spdp to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.discovery.sedp to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.behavior to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.behavior.reader to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.behavior.writer to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.structure.history to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.impl.spec.userdata to
            rtpstalk.tests;
}
