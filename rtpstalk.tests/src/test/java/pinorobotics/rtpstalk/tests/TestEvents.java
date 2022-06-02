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
package pinorobotics.rtpstalk.tests;

import id.xfunction.lang.XThread;
import id.xfunction.nio.file.XFiles;
import pinorobotics.rtpstalk.RtpsTalkClient;

/** @author lambdaprime intid@protonmail.com */
public class TestEvents {

    public static void waitForDiscoveredPublisher(String topic) throws Exception {
        XFiles.watchForStringInFile(LogUtils.LOG_FILE, "Discovered publisher for topic " + topic)
                .get();
    }

    public void waitNextSpdpCycle(RtpsTalkClient client) {
        // wait for next SPDP cycle
        var publishPeriod =
                client.getConfiguration()
                        .spdpDiscoveredParticipantDataPublishPeriod()
                        .plusSeconds(1)
                        .toMillis();
        XThread.sleep(publishPeriod);
    }
}
