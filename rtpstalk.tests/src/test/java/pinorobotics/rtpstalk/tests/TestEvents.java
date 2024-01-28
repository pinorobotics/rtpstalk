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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
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
        System.out.println("Actor discovered");
        return extractRemoteActorDetails(line);
    }

    private static RemoteActorDetails extractRemoteActorDetails(String line) {
        line =
                line.replaceAll(
                        """
                        .*"guidPrefix": . "value": "(.*)" ., "entityId": "(\\d+)" .* "reliabilityKind": "(.*)", "durabilityKind": "(.*)".*
                        """
                                .strip(),
                        "$1 $2 $3 $4");
        var a = line.split(" ");
        return new RemoteActorDetails(
                new Guid(a[0], a[1]),
                List.of(),
                ReliabilityQosPolicy.Kind.valueOf(a[2]),
                DurabilityQosPolicy.Kind.valueOf(a[3]));
    }

    public static void waitForDisposedParticipant(Guid participant) throws Exception {
        var str = "Writer marked participant " + participant.toString() + " as disposed";
        XFiles.watchForLineInFile(LogUtils.LOG_FILE, s -> s.contains(str), DELAY).get();
    }

    public static void waitForDisposedSubscriber(Guid reader) throws Exception {
        var str = "Reader " + reader + " marked subscription as disposed";
        XFiles.watchForLineInFile(LogUtils.LOG_FILE, s -> s.contains(str), DELAY).get();
    }

    @Test
    public void test_extractRemoteActorDetails() {
        var line =
                """
            Discovered Publisher for topic test with following details { "endpointGuid": { "guidPrefix": { "value": "010f43472b59302f01000000" }, "entityId": "00000103" }, "writerUnicastLocator": [{ "transportType": "LOCATOR_KIND_UDPv4", "port": "7415", "address": "/172.19.0.11" }], "reliabilityKind": "RELIABLE", "durabilityKind": "VOLATILE_DURABILITY_QOS" }
            """
                        .strip();
        Assertions.assertEquals(
                """
                { "endpointGuid": { "guidPrefix": { "value": "010f43472b59302f01000000" }, "entityId": "00000103" }, "writerUnicastLocator": [], "reliabilityKind": "RELIABLE", "durabilityKind": "VOLATILE_DURABILITY_QOS" }
                """
                        .strip(),
                extractRemoteActorDetails(line).toString());
    }
}
