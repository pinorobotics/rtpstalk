/*
 * Copyright 2020 rtpstalk project
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

import static java.util.stream.Collectors.joining;

import id.xfunction.ResourceUtils;
import id.xfunction.XByte;
import id.xfunction.concurrent.flow.CollectorSubscriber;
import id.xfunction.lang.XProcess;
import id.xfunction.lang.XThread;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.tests.LogUtils;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.TestEvents;
import pinorobotics.rtpstalk.tests.XAsserts;

/**
 * @author lambdaprime intid@protonmail.com
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsTalkClientPubSubPairsTests {

    private static final ResourceUtils resourceUtils = new ResourceUtils();
    private FastRtpsExamples tools;
    private RtpsTalkClient client;

    private record TestCase(
            int numberOfPubSubPairs,
            int numberOfMessages,
            RtpsTalkConfiguration config,
            boolean isSubscribeToFutureTopic,
            List<String> templates,
            List<String> subscribeTestTemplates,
            List<Runnable> validators,
            Map<FastRtpsEnvironmentVariable, String> publisherParameters) {}

    static Stream<TestCase> dataProvider() {
        return Stream.of(
                // 1
                new TestCase(
                        1,
                        5,
                        new RtpsTalkConfiguration.Builder()
                                .builtinEnpointsPort(8080)
                                .userEndpointsPort(8081)
                                .build(),
                        true,
                        List.of(
                                "service_startup.template",
                                "spdp_close.template",
                                "service_startup_ports_8080_8081.template"),
                        List.of("topic_subscriptions_manager_future_topic.template"),
                        List.of(RtpsTalkClientPubSubPairsTests::validateSedpClose),
                        Map.of()),
                // 2
                new TestCase(
                        1,
                        17,
                        new RtpsTalkConfiguration.Builder()
                                .builtinEnpointsPort(8080)
                                .userEndpointsPort(8081)
                                .build(),
                        false,
                        List.of(
                                "service_startup.template",
                                "spdp_close.template",
                                "service_startup_ports_8080_8081.template"),
                        List.of("topic_subscriptions_manager.template"),
                        List.of(RtpsTalkClientPubSubPairsTests::validateSedpClose),
                        Map.of()),
                // 3
                new TestCase(
                        1,
                        23,
                        new RtpsTalkConfiguration.Builder()
                                .networkInterface("lo")
                                .builtinEnpointsPort(8080)
                                .userEndpointsPort(8081)
                                .build(),
                        false,
                        List.of("service_startup_loopback_iface.template"),
                        List.of("topic_subscriptions_manager.template"),
                        List.of(
                                RtpsTalkClientPubSubPairsTests::validateSedpClose,
                                RtpsTalkClientPubSubPairsTests::validateSpdpLoopbackIface),
                        Map.of()),
                // 4
                new TestCase(
                        12,
                        5,
                        new RtpsTalkConfiguration.Builder()
                                .guidPrefix(TestConstants.TEST_GUID_PREFIX)
                                .builtinEnpointsPort(8080)
                                .userEndpointsPort(8081)
                                .build(),
                        false,
                        List.of(
                                "service_startup.template",
                                "spdp_close.template",
                                "service_startup_ports_8080_8081.template"),
                        List.of(),
                        List.of(RtpsTalkClientPubSubPairsTests::validateSedpClose),
                        Map.of(
                                FastRtpsEnvironmentVariable.DurabilityQosPolicyKind,
                                "TRANSIENT_LOCAL_DURABILITY_QOS")),
                // 5
                new TestCase(
                        1,
                        1,
                        new RtpsTalkConfiguration.Builder().build(),
                        false,
                        List.of(
                                "service_startup.template",
                                "spdp_close.template",
                                "service_startup_ports_default.template"),
                        List.of("topic_subscriptions_manager.template"),
                        List.of(RtpsTalkClientPubSubPairsTests::validateSedpClose),
                        Map.of(
                                FastRtpsEnvironmentVariable.DurabilityQosPolicyKind,
                                "VOLATILE_DURABILITY_QOS")));
    }

    @BeforeEach
    public void setup() throws IOException {
        LogUtils.setupLog();
        client =
                new RtpsTalkClient(
                        new RtpsTalkConfiguration.Builder()
                                .builtinEnpointsPort(8080)
                                .userEndpointsPort(8081)
                                .build());
        tools = new FastRtpsExamples();
    }

    @AfterEach
    public void clean() {
        client.close();
        tools.close();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void test_subscriber_publisher_pairs(TestCase testCase) throws Exception {
        client = new RtpsTalkClient(testCase.config);

        List<String> topics = generateTopicNames(testCase.numberOfPubSubPairs);
        var expectedData =
                generateMessages(testCase.numberOfMessages).stream()
                        .map(XByte::toHexPairs)
                        .collect(joining("\n"));
        var procs = new ArrayList<XProcess>();
        Runnable publishersRunner =
                () -> {
                    for (var topic : topics) {
                        var vars = new HashMap<>(testCase.publisherParameters());
                        vars.put(FastRtpsEnvironmentVariable.TopicName, topic);
                        procs.add(
                                tools.runHelloWorldExample(
                                        vars, "publisher", "" + testCase.numberOfMessages));
                    }
                };
        if (!testCase.isSubscribeToFutureTopic) {
            publishersRunner.run();
            client.start();
            TestEvents.waitForDiscoveredPublisher("HelloWorldTopic0");
        }
        var subscribers =
                Stream.generate(() -> new CollectorSubscriber<byte[]>(testCase.numberOfMessages))
                        .limit(testCase.numberOfPubSubPairs)
                        .toList();

        // subscribe all
        IntStream.range(0, testCase.numberOfPubSubPairs)
                .forEach(i -> client.subscribe(topics.get(i), "HelloWorld", subscribers.get(i)));

        if (testCase.isSubscribeToFutureTopic) publishersRunner.run();

        for (int i = 0; i < testCase.numberOfPubSubPairs; i++) {
            var dataReceived =
                    subscribers.get(i).getFuture().get().stream()
                            .map(XByte::toHexPairs)
                            .collect(joining("\n"));
            Assertions.assertEquals(expectedData, dataReceived);
        }

        client.close();

        var expectedStdout = tools.generateExpectedPublisherStdout(testCase.numberOfMessages);
        procs.forEach(proc -> Assertions.assertEquals(expectedStdout, proc.stdout()));

        assertTemplates(testCase.templates);
        assertTemplates(testCase.subscribeTestTemplates);
        assertValidators(testCase.validators);
    }

    private void assertValidators(List<Runnable> validators) {
        validators.forEach(Runnable::run);
    }

    private void assertTemplates(List<String> templates) {
        var log = LogUtils.readLogFile();
        templates.forEach(
                resourceName ->
                        XAsserts.assertMatches(
                                resourceUtils.readResource(getClass(), resourceName), log));
    }

    private List<String> generateTopicNames(int count) {
        return IntStream.range(0, count).mapToObj(i -> "HelloWorldTopic" + i).toList();
    }

    private List<byte[]> generateMessages(int count) {
        var out = new ArrayList<byte[]>();
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
            out.add(buf.array());
        }
        return out;
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void test_publisher_subscriber_pairs(TestCase testCase) throws Exception {
        client = new RtpsTalkClient(testCase.config);

        int numberOfPubSubPairs = testCase.numberOfPubSubPairs;
        List<String> topics = generateTopicNames(numberOfPubSubPairs);
        var procs = new ArrayList<XProcess>();
        Runnable subscribersRunner =
                () -> {
                    for (var topic : topics) {
                        var vars = new HashMap<>(testCase.publisherParameters());
                        vars.put(FastRtpsEnvironmentVariable.TopicName, topic);
                        procs.add(
                                tools.runHelloWorldExample(
                                        vars, "subscriber", "" + testCase.numberOfMessages));
                    }
                };

        if (testCase.isSubscribeToFutureTopic) {
            subscribersRunner.run();
        }

        var publishers =
                IntStream.range(0, numberOfPubSubPairs)
                        .mapToObj(
                                i -> {
                                    var publisher = new SubmissionPublisher<byte[]>();
                                    client.publish(topics.get(i), "HelloWorld", publisher);
                                    return publisher;
                                })
                        .toList();

        if (!testCase.isSubscribeToFutureTopic) subscribersRunner.run();

        var messagesToSubmit = generateMessages(testCase.numberOfMessages);
        for (int i = 0; i < numberOfPubSubPairs; i++) {
            var publisher = publishers.get(i);
            messagesToSubmit.forEach(publisher::submit);
        }

        for (int i = 0; i < numberOfPubSubPairs; i++) {
            var topic = topics.get(i);
            var expectedStdout =
                    tools.generateExpectedSubscriberStdout(testCase.numberOfMessages, topic);
            var actual = procs.get(i).stdout();
            System.out.println("Process for topic " + topic);
            System.out.println(actual);
            Assertions.assertEquals(expectedStdout, actual);
        }

        // close only after all subscribers received all data
        client.close();

        assertTemplates(testCase.templates);
        assertValidators(testCase.validators);
    }

    private void waitNextSpdpCycle() {
        // wait for next SPDP cycle
        var publishPeriod =
                client.getConfiguration()
                        .spdpDiscoveredParticipantDataPublishPeriod()
                        .plusSeconds(1)
                        .toMillis();
        XThread.sleep(publishPeriod);
    }

    private static void validateSedpClose() {
        var log = LogUtils.readLogFile();
        XAsserts.assertMatches(
                resourceUtils.readResourceAsList(
                        RtpsTalkClientPubSubPairsTests.class, "sedp_close.TEMPLATES"),
                log);
    }

    private static void validateSpdpLoopbackIface() {
        var log = LogUtils.readLogFile();
        Assertions.assertTrue(log.contains("Starting SPDP service on lo"));
        Assertions.assertFalse(log.contains("Starting SPDP service on eth0"));
    }
}
