/*
 * Copyright 2023 pinorobotics
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
package pinorobotics.rtpstalk.tests;

import id.xfunction.logging.XLogger;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class LogExtension implements BeforeEachCallback, AfterEachCallback {
    private static final XLogger LOGGER = XLogger.getLogger(LogExtension.class);

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        var logFileName =
                context.getTestMethod().map(Method::getName).orElse("unknown test")
                        + Optional.ofNullable(context.getDisplayName())
                                .map(s -> "_" + s.split(" ")[0])
                                .orElse("");
        context.getExecutionException().ifPresent(LOGGER::severe);
        LogUtils.archiveLog(logFileName);
    }

    @Override
    public void beforeEach(ExtensionContext arg0) throws Exception {
        LogUtils.setupLog();
    }
}
