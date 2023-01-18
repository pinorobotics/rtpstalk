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
package pinorobotics.rtpstalk.tests.integration;

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

/**
 * @author lambdaprime intid@protonmail.com
 */
public class FastRtpsExamples implements AutoCloseable {

    private static final String HELLOWORLDEXAMPLE_PATH =
            Paths.get("").toAbsolutePath().resolve("bld/HelloWorldExample").toString();
    private List<XProcess> procs = new ArrayList<>();

    public XProcess runHelloWorldExample(
            Map<FastRtpsEnvironmentVariable, String> env, String... args) {
        var argsList = new ArrayList<String>();
        argsList.add(HELLOWORLDEXAMPLE_PATH);
        argsList.addAll(List.of(args));
        var proc = new XExec(argsList).withEnvironmentVariables(toStringKeys(env)).start();
        procs.add(proc);
        proc.outputAsync(false);
        return proc;
    }

    private Map<String, String> toStringKeys(Map<FastRtpsEnvironmentVariable, String> env) {
        return env.entrySet().stream()
                .collect(toMap(e -> e.getKey().getVariableName(), Entry::getValue));
    }

    @Override
    public void close() {
        procs.forEach(proc -> proc.destroyAllForcibly());
    }

    public String generateExpectedPublisherStdout(int count) {
        var stdout = new StringBuilder();
        stdout.append(
                String.format(
                        """
                Starting\s
                Publisher running %s samples.
                Publisher matched
                """,
                        count));
        IntStream.rangeClosed(1, count)
                .forEach(
                        i ->
                                stdout.append(
                                        String.format(
                                                "Message: HelloWorld with index: %s SENT\n", i)));
        return stdout.toString().trim();
    }

    public String generateExpectedSubscriberStdout(int count, String topicName) {
        var stdout = new StringBuilder();
        stdout.append(
                String.format(
                        """
                Starting\s
                Using topic %s
                Subscriber running until %ssamples have been received
                Subscriber matched
                """,
                        topicName, count));
        IntStream.rangeClosed(1, count)
                .forEach(i -> stdout.append(String.format("Message HelloWorld %s RECEIVED\n", i)));
        return stdout.toString().trim();
    }

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
}
