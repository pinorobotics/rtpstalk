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
/*
 * Authors: - aeon_flux <aeon_flux@eclipso.ch>
 */
/**
 * @see <a href="https://github.com/pinorobotics/rtpstalk/releases">Download</a>
 * @see <a href="https://github.com/pinorobotics/rtpstalk">Github</a>
 */
module rtpstalk {
    exports pinorobotics.rtpstalk;
    exports pinorobotics.rtpstalk.spdp;
    exports pinorobotics.rtpstalk.messages to id.kineticstreamer;
    exports pinorobotics.rtpstalk.messages.submessages to id.kineticstreamer;
    exports pinorobotics.rtpstalk.messages.submessages.elements to id.kineticstreamer;

    requires id.xfunction;
    requires id.kineticstreamer;
}