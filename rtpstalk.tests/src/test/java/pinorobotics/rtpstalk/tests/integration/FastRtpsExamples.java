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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** @author lambdaprime intid@protonmail.com */
public class FastRtpsExamples implements AutoCloseable {

    private List<XProcess> procs = new ArrayList<>();

    public XProcess runHelloWorldPublisher() {
        return runHelloWorldPublisher(Map.of());
    }

    public XProcess runHelloWorldPublisher(Map<FastRtpsEnvironmentVariable, String> env) {
        var variables =
                env.entrySet().stream()
                        .map(e -> Map.entry(e.getKey().getVariableName(), e.getValue()))
                        .collect(toMap(Entry::getKey, Entry::getValue));
        var proc =
                new XExec("HelloWorldExample publisher").withEnvironmentVariables(variables).run();
        procs.add(proc);
        // When process writes to stdout it may get blocked until somebody
        // starts reading it. To avoid that we start reading immediately.
        proc.flush(false);
        return proc;
    }

    @Override
    public void close() {
        procs.forEach(proc -> proc.destroyAllForcibly());
    }

    public XProcess runHelloWorldSubscriber() {
        var proc = new XExec("HelloWorldExample subscriber").run();
        procs.add(proc);
        return proc;
    }
}
