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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.tests.LogUtils;
import pinorobotics.rtpstalk.tests.XAsserts;

/** @author lambdaprime intid@protonmail.com */
public class RtpsTalkClientTests {

    private static final ResourceUtils resourceUtils = new ResourceUtils();
    private FastRtpsExamples tools;
    private RtpsTalkClient client;

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

    static Stream<List> dataProvider() {
        return Stream.of(
                // 1
                List.of(
                        new RtpsTalkConfiguration.Builder()
                                .builtinEnpointsPort(8080)
                                .userEndpointsPort(8081)
                                .build(),
                        true,
                        "service_startup.template",
                        "spdp_close.template",
                        "service_startup_ports_8080_8081.template"),
                // 2
                List.of(
                        new RtpsTalkConfiguration.Builder()
                                .builtinEnpointsPort(8080)
                                .userEndpointsPort(8081)
                                .build(),
                        false,
                        "service_startup.template",
                        "spdp_close.template",
                        "service_startup_ports_8080_8081.template"),
                // 3
                List.of(
                        new RtpsTalkConfiguration.Builder().build(),
                        false,
                        "service_startup.template",
                        "spdp_close.template",
                        "service_startup_ports_default.template"));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void test_subscribe_happy(List testData) throws Exception {
        var config = (RtpsTalkConfiguration) testData.get(0);
        var isSubscribeToFutureTopic = (Boolean) testData.get(1);
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

        client = new RtpsTalkClient(config);

        XProcess proc = null;
        if (!isSubscribeToFutureTopic) {
            proc = tools.runHelloWorldPublisher();
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
        if (isSubscribeToFutureTopic) proc = tools.runHelloWorldPublisher();

        var dataReceived = future.get().toString();
        client.close();

        Assertions.assertEquals(
                resourceUtils.readResource(RtpsTalkClientTests.class, "HelloWorldTopic"),
                dataReceived);
        Assertions.assertEquals(
                resourceUtils.readResource(
                        RtpsTalkClientTests.class, "HelloWorldExample_publisher"),
                proc.stdoutAsString());
        var log = LogUtils.readLogFile();
        XAsserts.assertMatches(
                resourceUtils.readResourceAsList(RtpsTalkClientTests.class, "sedp_close.TEMPLATES"),
                log);
        for (int i = 2; i < testData.size(); i++) {
            var resourceName = (String) testData.get(i);
            System.out.println(resourceName);
            XAsserts.assertMatches(
                    resourceUtils.readResource(RtpsTalkClientTests.class, resourceName), log);
        }
    }

    @Test
    public void test_publish() throws Exception {
        var proc = tools.runHelloWorldSubscriber();
        var publisher = new SubmissionPublisher<byte[]>();
        client.publish("HelloWorldTopic", "HelloWorld", publisher);
        resourceUtils
                .readResourceAsStream(RtpsTalkClientTests.class, "HelloWorldTopic")
                .map(XByte::fromHexPairs)
                .forEach(publisher::submit);
        var actual = proc.stdout().limit(8).collect(joining("\n"));
        System.out.println(actual);
        client.close();

        Assertions.assertEquals(
                resourceUtils.readResource(
                        RtpsTalkClientTests.class, "HelloWorldExample_subscriber"),
                actual);

        var log = LogUtils.readLogFile();
        XAsserts.assertMatches(
                resourceUtils.readResource(RtpsTalkClientTests.class, "service_startup.template"),
                log);
        XAsserts.assertMatches(
                resourceUtils.readResource(RtpsTalkClientTests.class, "spdp_close.template"), log);
        XAsserts.assertMatches(
                resourceUtils.readResourceAsList(RtpsTalkClientTests.class, "sedp_close.TEMPLATES"),
                log);
    }
}
