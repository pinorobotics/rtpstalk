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
import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.lang.XProcess;
import id.xfunction.lang.XThread;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import pinorobotics.rtpstalk.tests.XAsserts;

/**
 * @author lambdaprime intid@protonmail.com
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsTalkClientSubscribeTests {

    private static final ResourceUtils resourceUtils = new ResourceUtils();
    private FastRtpsExamples tools;
    private RtpsTalkClient client;

    private record TestCase(
            int numberOfPubSubPairs,
            RtpsTalkConfiguration config,
            boolean isSubscribeToFutureTopic,
            List<String> templates,
            List<Runnable> validators,
            Map<FastRtpsEnvironmentVariable, String> publisherParameters) {}

    static Stream<TestCase> dataProvider() {
        return Stream.of(
                // 1
                new TestCase(
                        1,
                        new RtpsTalkConfiguration.Builder()
                                .builtinEnpointsPort(8080)
                                .userEndpointsPort(8081)
                                .build(),
                        true,
                        List.of(
                                "service_startup.template",
                                "spdp_close.template",
                                "service_startup_ports_8080_8081.template",
                                "topic_manager_future_topic.template"),
                        List.of(),
                        Map.of()),
                // 2
                new TestCase(
                        1,
                        new RtpsTalkConfiguration.Builder()
                                .builtinEnpointsPort(8080)
                                .userEndpointsPort(8081)
                                .build(),
                        false,
                        List.of(
                                "service_startup.template",
                                "spdp_close.template",
                                "service_startup_ports_8080_8081.template",
                                "topic_manager.template"),
                        List.of(),
                        Map.of()),
                // 3
                new TestCase(
                        1,
                        new RtpsTalkConfiguration.Builder()
                                .networkInterface("lo")
                                .builtinEnpointsPort(8080)
                                .userEndpointsPort(8081)
                                .build(),
                        false,
                        List.of(
                                "service_startup_loopback_iface.template",
                                "topic_manager.template"),
                        List.of(RtpsTalkClientSubscribeTests::validateSpdpLoopbackIface),
                        Map.of()),
                // 4
                new TestCase(
                        1,
                        new RtpsTalkConfiguration.Builder().build(),
                        false,
                        List.of(
                                "service_startup.template",
                                "spdp_close.template",
                                "service_startup_ports_default.template"),
                        List.of(),
                        Map.of(
                                FastRtpsEnvironmentVariable.DurabilityQosPolicyKind,
                                "TRANSIENT_LOCAL_DURABILITY_QOS")),
                // 5
                new TestCase(
                        1,
                        new RtpsTalkConfiguration.Builder().build(),
                        false,
                        List.of(
                                "service_startup.template",
                                "spdp_close.template",
                                "service_startup_ports_default.template",
                                "topic_manager.template"),
                        List.of(),
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
    public void test_publisher_subscriber_pairs(TestCase testCase) throws Exception {
        client = new RtpsTalkClient(testCase.config);

        List<String> topics =
                IntStream.range(0, testCase.numberOfPubSubPairs)
                        .mapToObj(i -> "HelloWorldTopic" + i)
                        .toList();
        var procs = new ArrayList<XProcess>();
        Runnable executor =
                () -> {
                    for (var topic : topics) {
                        var vars = new HashMap<>(testCase.publisherParameters());
                        vars.put(FastRtpsEnvironmentVariable.TopicName, topic);
                        procs.add(tools.runHelloWorldPublisher(vars));
                    }
                };
        if (!testCase.isSubscribeToFutureTopic) {
            executor.run();
            // subscribe to dummy topic to cause client to start all services
            // that way client will discover HelloWorldPublisher topic but not subscribe to it yet
            client.subscribe("topic", "type", new SimpleSubscriber<byte[]>());
            // wait for next SPDP cycle
            XThread.sleep(
                    client.getConfiguration()
                            .spdpDiscoveredParticipantDataPublishPeriod()
                            .plusSeconds(1)
                            .toMillis());
        }
        var subscribers =
                Stream.generate(() -> new CollectorSubscriber<byte[]>(5))
                        .limit(testCase.numberOfPubSubPairs)
                        .toList();
        IntStream.range(0, testCase.numberOfPubSubPairs)
                .forEach(i -> client.subscribe(topics.get(i), "HelloWorld", subscribers.get(i)));
        if (testCase.isSubscribeToFutureTopic) executor.run();

        for (int i = 0; i < testCase.numberOfPubSubPairs; i++) {
            var dataReceived =
                    subscribers.get(i).getFuture().get().stream()
                            .map(XByte::toHexPairs)
                            .collect(joining("\n"));
            Assertions.assertEquals(
                    resourceUtils.readResource(
                            RtpsTalkClientSubscribeTests.class, "HelloWorldTopic"),
                    dataReceived);
        }

        client.close();
        procs.forEach(
                proc ->
                        Assertions.assertEquals(
                                resourceUtils.readResource(
                                        RtpsTalkClientSubscribeTests.class,
                                        "HelloWorldExample_publisher"),
                                proc.stdoutAsString()));
        var log = LogUtils.readLogFile();
        XAsserts.assertMatches(
                resourceUtils.readResourceAsList(
                        RtpsTalkClientSubscribeTests.class, "sedp_close.TEMPLATES"),
                log);

        testCase.templates.forEach(
                resourceName ->
                        XAsserts.assertMatches(
                                resourceUtils.readResource(
                                        RtpsTalkClientSubscribeTests.class, resourceName),
                                log));
        testCase.validators.forEach(Runnable::run);
    }

    private static void validateSpdpLoopbackIface() {
        var log = LogUtils.readLogFile();
        Assertions.assertTrue(log.contains("Starting SPDP service on lo"));
        Assertions.assertFalse(log.contains("Starting SPDP service on eth0"));
    }
}
