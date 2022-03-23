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
 * @see <a href="https://github.com/pinorobotics/rtpstalk/releases">Download</a>
 * @see <a href="https://github.com/pinorobotics/rtpstalk">Github</a>
 * @author aeon_flux aeon_flux@eclipso.ch
 * @author lambdaprime intid@protonmail.com
 */
module rtpstalk {
    requires id.xfunction;
    requires id.kineticstreamer;

    exports pinorobotics.rtpstalk;
    exports pinorobotics.rtpstalk.messages to
            id.kineticstreamer,
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.messages.submessages to
            id.kineticstreamer,
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.messages.submessages.elements to
            id.kineticstreamer,
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.transport to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.transport.io to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.discovery.spdp to
            rtpstalk.tests;
    exports pinorobotics.rtpstalk.behavior to
            rtpstalk.tests;
}
