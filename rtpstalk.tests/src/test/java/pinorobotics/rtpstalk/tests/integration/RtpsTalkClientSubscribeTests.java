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
import id.xfunction.concurrent.flow.SimpleSubscriber;
import id.xfunction.lang.XProcess;
import id.xfunction.lang.XThread;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
            RtpsTalkConfiguration config,
            boolean isSubscribeToFutureTopic,
            List<String> templates,
            List<Runnable> validators,
            Map<FastRtpsEnvironmentVariable, String> publisherParameters) {}

    static Stream<TestCase> dataProvider() {
        return Stream.of(
                // 1
                new TestCase(
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
                                "TRANSIENT_LOCAL_DURABILITY_QOS")),
                // 5
                new TestCase(
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
    public void test_subscribe_happy(TestCase testCase) throws Exception {
        var future = new CompletableFuture<String>();
        var printer =
                new SimpleSubscriber<byte[]>() {
                    private List<String> buf = new ArrayList<>();

                    @Override
                    public void onNext(byte[] data) {
                        buf.add(XByte.toHexPairs(data));
                        if (buf.size() == 5) {
                            subscription.cancel();
                            future.complete(buf.stream().collect(joining("\n")));
                            return;
                        }
                        subscription.request(1);
                    }
                };

        client = new RtpsTalkClient(testCase.config);

        XProcess proc = null;
        if (!testCase.isSubscribeToFutureTopic) {
            proc = tools.runHelloWorldPublisher(testCase.publisherParameters());
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
        client.subscribe("HelloWorldTopic", "HelloWorld", printer);
        if (testCase.isSubscribeToFutureTopic)
            proc = tools.runHelloWorldPublisher(testCase.publisherParameters());

        var dataReceived = future.get().toString();
        client.close();

        Assertions.assertEquals(
                resourceUtils.readResource(RtpsTalkClientSubscribeTests.class, "HelloWorldTopic"),
                dataReceived);
        Assertions.assertEquals(
                resourceUtils.readResource(
                        RtpsTalkClientSubscribeTests.class, "HelloWorldExample_publisher"),
                proc.stdoutAsString());
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