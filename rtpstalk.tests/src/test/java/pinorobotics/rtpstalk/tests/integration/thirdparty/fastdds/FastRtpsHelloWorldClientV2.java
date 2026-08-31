/*
 * Copyright 2022 pinorobotics
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
package pinorobotics.rtpstalk.tests.integration.thirdparty.fastdds;

import static java.util.stream.Collectors.toMap;

import id.xfunction.lang.XExec;
import id.xfunction.lang.XProcess;
import id.xfunction.logging.XLogger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.IntStream;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.integration.thirdparty.HelloWorldClient;
import pinorobotics.rtpstalk.tests.integration.thirdparty.HelloWorldExampleVariable;
import pinorobotics.rtpstalk.tests.integration.thirdparty.PubSubPairsTests;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class FastRtpsHelloWorldClientV2 implements HelloWorldClient {
    private static final XLogger LOGGER = XLogger.getLogger(PubSubPairsTests.class);
    private static final String HELLOWORLDEXAMPLE_PATH =
            EXECUTABLE_HOME.resolve("fastdds/v2/HelloWorldExample").toString();
    private List<XProcess> procs = new ArrayList<>();

    /** By default 10 messages are published */
    @Override
    public XProcess runHelloWorldExample(Map<HelloWorldExampleVariable, String> config) {
        var argsList = new ArrayList<String>();
        argsList.add(HELLOWORLDEXAMPLE_PATH);
        if (config.containsKey(HelloWorldExampleVariable.RunPublisher)) {
            argsList.add("publisher");
        } else if (config.containsKey(HelloWorldExampleVariable.RunSubscriber)) {
            argsList.add("subscriber");
        } else {
            throw new UnsupportedOperationException();
        }
        if (config.containsKey(HelloWorldExampleVariable.NumberOfMesages))
            argsList.add(config.get(HelloWorldExampleVariable.NumberOfMesages));
        if (config.containsKey(HelloWorldExampleVariable.SleepBetweenMessagesInMillis))
            argsList.add(config.get(HelloWorldExampleVariable.SleepBetweenMessagesInMillis));
        LOGGER.info("Running command: " + argsList);
        Map<String, String> env = new HashMap<>(toStringKeys(config));
        env.put(
                "LD_LIBRARY_PATH",
                Objects.requireNonNull(
                        System.getenv("FASTDDS_V2_LIBS"),
                        "FASTDDS_V2_LIBS env variable is missing"));
        var proc = new XExec(argsList).withEnvironmentVariables(env).start();
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
        procs.forEach(
                proc -> {
                    LOGGER.info("Destroying proc");
                    proc.destroyAllForcibly();
                });
    }

    @Override
    public String generateExpectedPublisherStdout(int count, String topicName) {
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

    @Override
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
        return "FastRtpsHelloWorldExampleV2";
    }
}
