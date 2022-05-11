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
import java.io.IOException;
import java.util.concurrent.SubmissionPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
