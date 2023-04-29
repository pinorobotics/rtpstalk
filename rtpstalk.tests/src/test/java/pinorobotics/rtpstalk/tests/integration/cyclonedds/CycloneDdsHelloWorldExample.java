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
package pinorobotics.rtpstalk.tests.integration.cyclonedds;

import static java.util.stream.Collectors.toMap;

import id.xfunction.lang.XExec;
import id.xfunction.lang.XProcess;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.integration.HelloWorldExample;
import pinorobotics.rtpstalk.tests.integration.HelloWorldExampleVariable;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class CycloneDdsHelloWorldExample implements HelloWorldExample {

    private static final String PUBLISHER_PATH =
            Paths.get("").toAbsolutePath().resolve("bld/cyclonedds/HelloworldPublisher").toString();
    private static final String SUBSCRIBER_PATH =
            Paths.get("")
                    .toAbsolutePath()
                    .resolve("bld/cyclonedds/HelloworldSubscriber")
                    .toString();
    private List<XProcess> procs = new ArrayList<>();

    @Override
    public XProcess runHelloWorldExample(Map<HelloWorldExampleVariable, String> env) {
        var argsList = new ArrayList<String>();
        if (env.containsKey(HelloWorldExampleVariable.RunPublisher)) {
            argsList.add(PUBLISHER_PATH);
        } else if (env.containsKey(HelloWorldExampleVariable.RunSubscriber)) {
            argsList.add(SUBSCRIBER_PATH);
        } else {
            throw new UnsupportedOperationException();
        }
        if (env.containsKey(HelloWorldExampleVariable.NumberOfMesages)) {
            argsList.add(env.get(HelloWorldExampleVariable.NumberOfMesages));
        }
        System.out.println("Running command: " + argsList);
        var proc = new XExec(argsList).withEnvironmentVariables(toStringKeys(env)).start();
        procs.add(proc);
        proc.forwardOutputAsync(false);
        return proc;
    }

    private Map<String, String> toStringKeys(Map<HelloWorldExampleVariable, String> env) {
        return env.entrySet().stream()
                .collect(toMap(e -> e.getKey().getVariableName(), Entry::getValue));
    }

    @Override
    public void close() {
        procs.forEach(proc -> proc.destroyAllForcibly());
    }

    @Override
    public String generateExpectedPublisherStdout(int count, String topicName) {
        var stdout = new StringBuilder();
        stdout.append(
                """
                Num of samples: %s
                Topic name: %s
                === [Publisher]  Waiting for a reader to be discovered ...
                """
                        .formatted(count, topicName));
        IntStream.rangeClosed(1, count)
                .forEach(
                        i ->
                                stdout.append(
                                        """
                                        === [Publisher]  Writing :$
                                        Message (%s, HelloWorld)
                                        """
                                                .replace("$", " ")
                                                .formatted(i)));
        return stdout.toString().trim();
    }

    @Override
    public String generateExpectedSubscriberStdout(int count, String topicName) {
        var stdout = new StringBuilder();
        stdout.append(
                """
                Num of samples: %s

                === [Subscriber] Waiting for a sample ...
                """
                        .formatted(count));
        IntStream.rangeClosed(1, count)
                .forEach(
                        i ->
                                stdout.append(
                                        """
                                === [Subscriber] Received : Message (%s, HelloWorld)
                                """
                                                .formatted(i)));
        return stdout.toString().trim();
    }

    @Override
    public List<RtpsTalkDataMessage> generateMessages(int count) {
        var out = new ArrayList<RtpsTalkDataMessage>();
        var text = "HelloWorld";
        for (int i = 1; i <= count; i++) {
            var buf =
                    ByteBuffer.allocate(
                            // unsigned long index
                            Integer.BYTES
                                    // string message;
                                    + Integer.BYTES // length
                                    + text.length()
                                    + 2 /*null byte + padding*/);
            buf.putInt(Integer.reverseBytes(i));
            buf.putInt(Integer.reverseBytes(text.length() + 1));
            buf.put(text.getBytes());
            out.add(new RtpsTalkDataMessage(buf.array()));
        }
        return out;
    }

    public List<Integer> extractMessageIds(String stdout) {
        return stdout.lines()
                .skip(4)
                .map(line -> line.replaceAll("Message HelloWorld (\\d+) RECEIVED", "$1"))
                .map(Integer::parseInt)
                .toList();
    }

    @Override
    public String toString() {
        return "CycloneDdsHelloWorldExample";
    }
}
