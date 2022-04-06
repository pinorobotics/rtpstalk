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
package pinorobotics.rtpstalk.tests.transport.io;

import static pinorobotics.rtpstalk.tests.TestConstants.*;

import id.xfunction.ResourceUtils;
import java.util.List;
import java.util.stream.Stream;

/** @author lambdaprime intid@protonmail.com */
public class DataProviders {

    private static final ResourceUtils resourceUtils = new ResourceUtils();

    public static Stream<List> rtpsMessageConversion() {
        return Stream.of(
                List.of(
                        resourceUtils.readResource(DataProviders.class, "test1"),
                        TEST_MESSAGE_INFODST_ACKNACK),
                List.of(
                        resourceUtils.readResource(DataProviders.class, "test_submessages_padding"),
                        TEST_MESSAGE_INFODST_DATA_PADDING));
    }
}
