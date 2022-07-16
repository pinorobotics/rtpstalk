/*
 * Copyright 2020 kineticstreamer project
 *
 * Website: https://github.com/lambdaprime/kineticstreamer
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
/*
 * @author lambdaprime intid@protonmail.com
 */
open module rtpstalk.tests {
    exports pinorobotics.rtpstalk.tests.spec.transport.io;

    requires rtpstalk;
    requires id.kineticstreamer;
    requires id.xfunction;
    requires pubsubtests;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
}
