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

import id.xfunction.nio.file.XFiles;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.topics.ActorDetails;
import pinorobotics.rtpstalk.impl.topics.RemoteActorDetails;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class TestEvents {

    private static final Duration DELAY = Duration.ofMillis(100);

    public static RemoteActorDetails waitForDiscoveredActor(
            String topic, ActorDetails.Type actorType) throws Exception {
        var regexp =
                Pattern.compile(".*Discovered " + actorType + " for topic " + topic + ".*")
                        .asMatchPredicate();
        var line = XFiles.watchForLineInFile(LogUtils.LOG_FILE, regexp, DELAY).get();
        return extractRemoteActorDetails(line);
    }

    private static RemoteActorDetails extractRemoteActorDetails(String line) {
        line =
                line.replaceAll(
                        ".*\"guidPrefix\": . .value.: \"(.*)\" ., .entityId.: \"(\\d+)\" .*"
                                + " \"reliabilityKind\": \"(.*)\".*",
                        "$1 $2 $3");
        var a = line.split(" ");
        return new RemoteActorDetails(
                new Guid(a[0], a[1]), List.of(), ReliabilityQosPolicy.Kind.valueOf(a[2]));
    }

    public static void waitForDisposedParticipant(Guid participant) throws Exception {
        var str = "Writer marked participant " + participant.toString() + " as disposed";
        XFiles.watchForLineInFile(LogUtils.LOG_FILE, s -> s.contains(str), DELAY).get();
    }

    public static void waitForDisposedSubscriber(Guid reader) throws Exception {
        var str = "Reader " + reader + " marked subscription as disposed";
        XFiles.watchForLineInFile(LogUtils.LOG_FILE, s -> s.contains(str), DELAY).get();
    }
}
