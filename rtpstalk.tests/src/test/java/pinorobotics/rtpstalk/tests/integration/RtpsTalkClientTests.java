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
import id.xfunction.lang.XThread;
import id.xfunction.logging.XLogger;
import id.xfunction.text.WildcardMatcher;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;

/** @author lambdaprime intid@protonmail.com */
public class RtpsTalkClientTests {

    private static final ResourceUtils resourceUtils = new ResourceUtils();
    private FastRtpsExamples tools = new FastRtpsExamples();
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
        // client.close();
        tools.close();
    }

    @Test
    public void test_subscribe_to_existing() throws Exception {
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
        var proc = tools.runHelloWorldPublisher();
        // give 1 sec to start
        XThread.sleep(1000);
        client.subscribe("HelloWorldTopic", "HelloWorld", printer);

        Assertions.assertEquals(
                resourceUtils.readResource(RtpsTalkClientTests.class, "HelloWorldTopic"),
                future.get().toString());
        Assertions.assertEquals(
                resourceUtils.readResource(
                        RtpsTalkClientTests.class, "HelloWorldExample_publisher"),
                proc.stdoutAsString());

        var log =
                Files.readString(
                        Paths.get(System.getProperty("java.io.tmpdir"), "rtpstalk-test.log"));
        Assertions.assertTrue(
                new WildcardMatcher(
                                resourceUtils.readResource(
                                        RtpsTalkClientTests.class, "service_startup"))
                        .matches(log));
    }
}
