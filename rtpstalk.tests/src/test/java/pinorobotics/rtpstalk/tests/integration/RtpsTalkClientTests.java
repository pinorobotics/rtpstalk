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
import id.xfunction.function.Unchecked;
import id.xfunction.lang.XProcess;
import id.xfunction.logging.XLogger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SubmissionPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.tests.XAsserts;

/** @author lambdaprime intid@protonmail.com */
public class RtpsTalkClientTests {

    private static final ResourceUtils resourceUtils = new ResourceUtils();
    private FastRtpsExamples tools;
    private RtpsTalkClient client;

    @BeforeAll
    public static void setupAll() {
        XLogger.load("rtpstalk-test.properties");
    }

    @BeforeEach
    public void setup() {
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
    @CsvSource({"true", "false"})
    public void test_subscribe_happy(boolean isPublisherExist) throws Exception {
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

        XProcess proc = null;
        if (isPublisherExist) proc = tools.runHelloWorldPublisher();
        client.subscribe("HelloWorldTopic", "HelloWorld", printer);
        if (!isPublisherExist) proc = tools.runHelloWorldPublisher();

        var dataReceived = future.get().toString();
        client.close();

        Assertions.assertEquals(
                resourceUtils.readResource(RtpsTalkClientTests.class, "HelloWorldTopic"),
                dataReceived);
        Assertions.assertEquals(
                resourceUtils.readResource(
                        RtpsTalkClientTests.class, "HelloWorldExample_publisher"),
                proc.stdoutAsString());

        var log = readLogFile();
        XAsserts.assertMatches(
                resourceUtils.readResource(RtpsTalkClientTests.class, "service_startup.template"),
                log);
        XAsserts.assertMatches(
                resourceUtils.readResource(RtpsTalkClientTests.class, "spdp_close.template"), log);
        XAsserts.assertMatches(
                resourceUtils.readResourceAsList(RtpsTalkClientTests.class, "sedp_close.TEMPLATES"),
                log);
    }

    private String readLogFile() {
        return Unchecked.get(
                () ->
                        Files.readString(
                                Paths.get(
                                        System.getProperty("java.io.tmpdir"),
                                        "rtpstalk-test.log")));
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

        var log = readLogFile();
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
