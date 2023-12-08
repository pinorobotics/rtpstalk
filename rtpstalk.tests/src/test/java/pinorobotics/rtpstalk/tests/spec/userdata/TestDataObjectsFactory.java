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
package pinorobotics.rtpstalk.tests.spec.userdata;

import id.xfunction.concurrent.SameThreadExecutorService;
import id.xfunction.logging.TracingToken;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import pinorobotics.rtpstalk.impl.RtpsTalkConfigurationInternal;
import pinorobotics.rtpstalk.impl.qos.ReaderQosPolicySet;
import pinorobotics.rtpstalk.impl.spec.behavior.LocalOperatingEntities;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.userdata.DataObjectsFactory;
import pinorobotics.rtpstalk.impl.spec.userdata.DataReader;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class TestDataObjectsFactory extends DataObjectsFactory {

    private List<TestDataReader> dataReaders = new ArrayList<>();
    private Executor executor;
    private int maxBufferCapacity;

    public TestDataObjectsFactory() {
        this(true);
    }

    public TestDataObjectsFactory(boolean isSameThreadExecutor) {
        this(
                isSameThreadExecutor ? new SameThreadExecutorService() : ForkJoinPool.commonPool(),
                isSameThreadExecutor ? 1 : Flow.defaultBufferSize());
    }

    public TestDataObjectsFactory(Executor executor, int maxBufferCapacity) {
        this.executor = executor;
        this.maxBufferCapacity = maxBufferCapacity;
    }

    @Override
    public DataReader newDataReader(
            RtpsTalkConfigurationInternal config,
            TracingToken tracingToken,
            Executor publisherExecutor,
            LocalOperatingEntities operatingEntities,
            EntityId eid,
            ReaderQosPolicySet subscriberQosPolicy) {
        var reader =
                new TestDataReader(
                        config, tracingToken, operatingEntities, eid, executor, maxBufferCapacity);
        dataReaders.add(reader);
        return reader;
    }

    public List<TestDataReader> getReaders() {
        return dataReaders;
    }
}
