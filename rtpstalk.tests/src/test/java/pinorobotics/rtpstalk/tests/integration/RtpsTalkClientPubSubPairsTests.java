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
import id.xfunction.concurrent.flow.FixedCollectorSubscriber;
import id.xfunction.lang.XProcess;
import id.xfunction.text.Substitutor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pinorobotics.rtpstalk.RtpsTalkClient;
import pinorobotics.rtpstalk.RtpsTalkConfiguration;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.spec.messages.Guid;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityKind;
import pinorobotics.rtpstalk.impl.topics.ActorDetails;
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage;
import pinorobotics.rtpstalk.tests.LogUtils;
import pinorobotics.rtpstalk.tests.TestConstants;
import pinorobotics.rtpstalk.tests.TestEvents;
import pinorobotics.rtpstalk.tests.TestUtils;
import pinorobotics.rtpstalk.tests.XAsserts;
import pinorobotics.rtpstalk.tests.integration.cyclonedds.CycloneDdsHelloWorldExample;
import pinorobotics.rtpstalk.tests.integration.fastdds.FastRtpsHelloWorldExample;

/**
 * @author lambdaprime intid@protonmail.com
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class RtpsTalkClientPubSubPairsTests {

    private static SdkMeterProvider sdkMeterProvider;
    private HelloWorldExample helloWorldExample;
    private RtpsTalkClient client;

    private enum TestCondition {
        LOCAL_SUBSCRIBER,
        LOCAL_PUBLISHER,
    }

    private record TestCase(
            HelloWorldExample helloWorldExample,
            int numberOfPubSubPairs,
            int numberOfMessages,
            RtpsTalkConfiguration config,
            boolean isSubscribeToFutureTopic,
            List<String> templates,
            Map<TestCondition, List<String>> conditionalTemplates,
            List<Runnable> validators,
            Map<TestCondition, Map<HelloWorldExampleVariable, String>>
                    remoteParticipantParameters) {
        TestCase(
                HelloWorldExample helloWorldExample,
                int numberOfPubSubPairs,
                int numberOfMessages,
                RtpsTalkConfiguration config,
                boolean isSubscribeToFutureTopic,
                List<String> templates,
                Map<TestCondition, List<String>> conditionalTemplates,
                List<Runnable> validators) {
            this(
                    helloWorldExample,
                    numberOfPubSubPairs,
                    numberOfMessages,
                    config,
                    isSubscribeToFutureTopic,
                    templates,
                    conditionalTemplates,
                    validators,
                    Map.of());
        }
    }

    static Stream<TestCase> dataProvider() {
        var testCases = new ArrayList<TestCase>();
        for (var helloWorldExample :
                List.of(new CycloneDdsHelloWorldExample(), new FastRtpsHelloWorldExample())) {
            Stream.of(
                            // Test case 1
                            new TestCase(
                                    helloWorldExample,
                                    1,
                                    5,
                                    new RtpsTalkConfiguration.Builder()
                                            .builtinEnpointsPort(8080)
                                            .userEndpointsPort(8081)
                                            .build(),
                                    true,
                                    List.of(
                                            "service_close.template",
                                            "service_startup_ports_8080_8081.template",
                                            "ParticipantsRegistry.template"),
                                    Map.of(
                                            TestCondition.LOCAL_SUBSCRIBER,
                                            List.of(
                                                    "topic_subscriptions_manager_future_topic.template")),
                                    List.of(
                                            () ->
                                                    validateAcrossNetworkInterfaces(
                                                            "service_startup.template"),
                                            () ->
                                                    validateAcrossNetworkInterfaces(
                                                            "spdp_close.template"),
                                            RtpsTalkClientPubSubPairsTests::validateSedpClose,
                                            LogUtils::validateNoExceptions)),
                            // Test case 2
                            new TestCase(
                                    helloWorldExample,
                                    1,
                                    17,
                                    new RtpsTalkConfiguration.Builder()
                                            .builtinEnpointsPort(8080)
                                            .userEndpointsPort(8081)
                                            .build(),
                                    false,
                                    List.of("service_startup_ports_8080_8081.template"),
                                    Map.of(
                                            TestCondition.LOCAL_SUBSCRIBER,
                                            List.of("topic_subscriptions_manager.template")),
                                    List.of(
                                            () ->
                                                    validateAcrossNetworkInterfaces(
                                                            "service_startup.template"),
                                            () ->
                                                    validateAcrossNetworkInterfaces(
                                                            "spdp_close.template"),
                                            RtpsTalkClientPubSubPairsTests::validateSedpClose,
                                            LogUtils::validateNoExceptions)),
                            // Test case 3
                            new TestCase(
                                    helloWorldExample,
                                    1,
                                    50,
                                    new RtpsTalkConfiguration.Builder()
                                            .networkInterface("lo")
                                            .builtinEnpointsPort(8080)
                                            .userEndpointsPort(8081)
                                            .build(),
                                    false,
                                    List.of("service_startup_loopback_iface.template"),
                                    Map.of(
                                            TestCondition.LOCAL_SUBSCRIBER,
                                            List.of("topic_subscriptions_manager.template")),
                                    List.of(
                                            RtpsTalkClientPubSubPairsTests::validateSedpClose,
                                            LogUtils::validateNoExceptions)),
                            // Test case 4
                            new TestCase(
                                    helloWorldExample,
                                    12,
                                    5,
                                    new RtpsTalkConfiguration.Builder()
                                            .guidPrefix(TestConstants.TEST_GUID_PREFIX.value)
                                            .builtinEnpointsPort(8080)
                                            .userEndpointsPort(8081)
                                            .build(),
                                    false,
                                    List.of("service_startup_ports_8080_8081.template"),
                                    Map.of(),
                                    List.of(
                                            () ->
                                                    validateAcrossNetworkInterfaces(
                                                            "service_startup.template"),
                                            () ->
                                                    validateAcrossNetworkInterfaces(
                                                            "spdp_close.template"),
                                            RtpsTalkClientPubSubPairsTests::validateSedpClose),
                                    Map.of(
                                            TestCondition.LOCAL_PUBLISHER,
                                            Map.of(
                                                    HelloWorldExampleVariable
                                                            .DurabilityQosPolicyKind,
                                                    "TRANSIENT_LOCAL_DURABILITY_QOS"))),
                            // Test case 5
                            new TestCase(
                                    helloWorldExample,
                                    1,
                                    1,
                                    new RtpsTalkConfiguration.Builder().build(),
                                    false,
                                    List.of("service_startup_ports_default.template"),
                                    Map.of(
                                            TestCondition.LOCAL_SUBSCRIBER,
                                            List.of("topic_subscriptions_manager.template")),
                                    List.of(
                                            () ->
                                                    validateAcrossNetworkInterfaces(
                                                            "service_startup.template"),
                                            () ->
                                                    validateAcrossNetworkInterfaces(
                                                            "spdp_close.template"),
                                            RtpsTalkClientPubSubPairsTests::validateSedpClose),
                                    Map.of(
                                            TestCondition.LOCAL_PUBLISHER,
                                            Map.of(
                                                    HelloWorldExampleVariable
                                                            .DurabilityQosPolicyKind,
                                                    "VOLATILE_DURABILITY_QOS"))))
                    .forEach(testCases::add);
        }
        return testCases.stream();
    }

    @BeforeAll
    public static void setupAll() {
        sdkMeterProvider = TestUtils.setupMetrics();
    }

    @AfterAll
    public static void cleanupAll() {
        sdkMeterProvider.close();
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
    }

    @AfterEach
    public void clean() {
        client.close();
        helloWorldExample.close();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void test_local_subscriber_remote_publisher_pairs(TestCase testCase) throws Exception {
        helloWorldExample = testCase.helloWorldExample();
        client = new RtpsTalkClient(testCase.config);

        List<String> topics = generateTopicNames(testCase.numberOfPubSubPairs);
        var expectedData =
                helloWorldExample.generateMessages(testCase.numberOfMessages).stream()
                        .map(RtpsTalkDataMessage::data)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(XByte::toHexPairs)
                        .collect(joining("\n"));
        var procs = new ArrayList<XProcess>();
        Runnable publishersRunner =
                () -> {
                    for (var topic : topics) {
                        var vars =
                                new HashMap<>(
                                        testCase.remoteParticipantParameters()
                                                .getOrDefault(
                                                        TestCondition.LOCAL_SUBSCRIBER, Map.of()));
                        vars.put(HelloWorldExampleVariable.TopicName, topic);
                        vars.put(HelloWorldExampleVariable.RunPublisher, "true");
                        vars.put(
                                HelloWorldExampleVariable.NumberOfMesages,
                                "" + testCase.numberOfMessages);
                        procs.add(helloWorldExample.runHelloWorldExample(vars));
                    }
                };
        Guid publisherGuid = null;
        if (!testCase.isSubscribeToFutureTopic) {
            publishersRunner.run();
            client.start();
            publisherGuid =
                    TestEvents.waitForDiscoveredActor(
                                    "HelloWorldTopic0", ActorDetails.Type.Publisher)
                            .endpointGuid();
        }
        var subscribers =
                Stream.generate(
                                () ->
                                        new FixedCollectorSubscriber<>(
                                                new ArrayList<RtpsTalkDataMessage>(),
                                                testCase.numberOfMessages))
                        .limit(testCase.numberOfPubSubPairs)
                        .toList();

        // subscribe all
        var entityIds =
                IntStream.range(0, testCase.numberOfPubSubPairs)
                        .map(i -> client.subscribe(topics.get(i), "HelloWorld", subscribers.get(i)))
                        .toArray();

        if (!testCase.isSubscribeToFutureTopic) {
            // there is no ordering in topics discovering so entityId assignments
            // are at random and we sort them
            Arrays.sort(entityIds);
        }

        if (testCase.isSubscribeToFutureTopic) {
            publishersRunner.run();
            publisherGuid =
                    TestEvents.waitForDiscoveredActor(
                                    "HelloWorldTopic0", ActorDetails.Type.Publisher)
                            .endpointGuid();
        }

        for (int i = 0; i < testCase.numberOfPubSubPairs; i++) {
            var dataReceived =
                    subscribers.get(i).getFuture().get().stream()
                            .map(RtpsTalkDataMessage::data)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(XByte::toHexPairs)
                            .collect(joining("\n"));
            Assertions.assertEquals(expectedData, dataReceived);
        }

        TestEvents.waitForDisposedParticipant(
                new Guid(publisherGuid.guidPrefix, EntityId.Predefined.ENTITYID_PARTICIPANT));
        client.close();

        for (int i = 0; i < testCase.numberOfPubSubPairs; i++) {
            var topic = topics.get(i);
            var expectedStdout =
                    helloWorldExample.generateExpectedPublisherStdout(
                            testCase.numberOfMessages, topic);
            var actual = procs.get(i).stdout();
            Assertions.assertEquals(expectedStdout, actual);
        }

        assertTemplates(testCase.templates);
        Optional.ofNullable(testCase.conditionalTemplates().get(TestCondition.LOCAL_SUBSCRIBER))
                .ifPresent(this::assertTemplates);
        assertValidators(testCase.validators);

        // test that readers entity ids are properly assigned
        System.out.println(Arrays.toString(entityIds));
        for (int i = 0; i < entityIds.length; i++) {
            var entityId = new EntityId(i + 1, EntityKind.READER_NO_KEY);
            Assertions.assertEquals(entityId.value, entityIds[i]);
        }
    }

    private void assertValidators(List<Runnable> validators) {
        validators.forEach(Runnable::run);
    }

    private void assertTemplates(List<String> templates) {
        System.out.println("Asserting templates " + templates);
        var log = LogUtils.readLogFile();
        templates.forEach(resourceName -> XAsserts.assertMatches(getClass(), resourceName, log));
    }

    private List<String> generateTopicNames(int count) {
        return IntStream.range(0, count).mapToObj(i -> "HelloWorldTopic" + i).toList();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void test_local_publisher_remote_subscriber_pairs(TestCase testCase) throws Exception {
        helloWorldExample = testCase.helloWorldExample();
        client = new RtpsTalkClient(testCase.config);

        int numberOfPubSubPairs = testCase.numberOfPubSubPairs;
        List<String> topics = generateTopicNames(numberOfPubSubPairs);
        var procs = new ArrayList<XProcess>();
        Runnable subscribersRunner =
                () -> {
                    for (var topic : topics) {
                        var vars =
                                new HashMap<>(
                                        testCase.remoteParticipantParameters()
                                                .getOrDefault(
                                                        TestCondition.LOCAL_PUBLISHER, Map.of()));
                        vars.put(HelloWorldExampleVariable.TopicName, topic);
                        vars.put(HelloWorldExampleVariable.RunSubscriber, "true");
                        vars.put(
                                HelloWorldExampleVariable.NumberOfMesages,
                                "" + testCase.numberOfMessages);
                        procs.add(helloWorldExample.runHelloWorldExample(vars));
                    }
                };

        Guid readerGuid = null;
        if (testCase.isSubscribeToFutureTopic) {
            subscribersRunner.run();
            // start client to do discovery of subscriber before the actual publisher will
            // be available
            client.start();
            readerGuid =
                    TestEvents.waitForDiscoveredActor(
                                    "HelloWorldTopic0", ActorDetails.Type.Subscriber)
                            .endpointGuid();
        }

        var publishers =
                IntStream.range(0, numberOfPubSubPairs)
                        .mapToObj(
                                i -> {
                                    var publisher = new SubmissionPublisher<RtpsTalkDataMessage>();
                                    client.publish(topics.get(i), "HelloWorld", publisher);
                                    return publisher;
                                })
                        .toList();

        if (!testCase.isSubscribeToFutureTopic) {
            subscribersRunner.run();
            readerGuid =
                    TestEvents.waitForDiscoveredActor(
                                    "HelloWorldTopic0", ActorDetails.Type.Subscriber)
                            .endpointGuid();
        }

        var messagesToSubmit = helloWorldExample.generateMessages(testCase.numberOfMessages);
        for (int i = 0; i < numberOfPubSubPairs; i++) {
            var publisher = publishers.get(i);
            messagesToSubmit.forEach(publisher::submit);
        }

        for (int i = 0; i < numberOfPubSubPairs; i++) {
            var topic = topics.get(i);
            var expectedStdout =
                    helloWorldExample.generateExpectedSubscriberStdout(
                            testCase.numberOfMessages, topic);
            var actual = procs.get(i).stdout();
            System.out.println("Process for topic " + topic);
            System.out.println(actual);
            Assertions.assertEquals(expectedStdout, actual);
        }

        TestEvents.waitForDisposedSubscriber(readerGuid);

        // close only after all subscribers received all data
        client.close();

        assertTemplates(testCase.templates);
        assertValidators(testCase.validators);
    }

    private static void validateAcrossNetworkInterfaces(String templateResourceName) {
        var log = LogUtils.readLogFile();
        ResourceUtils resourceUtils = new ResourceUtils();
        var template =
                resourceUtils.readResource(
                        RtpsTalkClientPubSubPairsTests.class, templateResourceName);
        for (var iface : InternalUtils.getInstance().listAllNetworkInterfaces()) {
            var expectedTemplate =
                    new Substitutor().substitute(template, Map.of("${IFACE}", iface.getName()));
            XAsserts.assertMatches(expectedTemplate, log);
        }
    }

    private static void validateSedpClose() {
        var log = LogUtils.readLogFile();
        XAsserts.assertMatchesAll(
                RtpsTalkClientPubSubPairsTests.class, "sedp_close.TEMPLATES", log);
    }
}
