/*
 * Copyright 2022 pinorobotics
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

import id.xfunction.function.Unchecked;
import id.xfunction.logging.XLogger;
import id.xfunction.nio.file.XFiles;
import id.xfunction.text.Ellipsizer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class LogUtils {

    public static final Path LOG_FILE =
            XFiles.TEMP_FOLDER.map(p -> p.resolve("rtpstalk-test.log")).orElseThrow();
    private static final Path LOG_ARCHIVE_FOLDER =
            Unchecked.get(() -> Files.createTempDirectory("rtpstalk-tests"));
    private static final Ellipsizer ELLIPSIZER = new Ellipsizer(60);

    public static void setupLog() {
        XLogger.reset();
        Unchecked.run(() -> Files.deleteIfExists(LOG_FILE));
        // since we deleted the log file we need to reconfigure logger
        XLogger.load("rtpstalk-test.properties");
    }

    public static void archiveLog(String testName) {
        // make sure we stop writing to log file before moving it
        XLogger.reset();
        try {
            Files.move(LOG_FILE, LOG_ARCHIVE_FOLDER.resolve(testName + System.currentTimeMillis()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readLogFile() {
        return Unchecked.get(() -> Files.readString(LOG_FILE));
    }

    public static void validateNoExceptions() {
        var log = LogUtils.readLogFile();
        Assertions.assertFalse(log.contains("Exception"), "Exceptions found in the log");
        Assertions.assertFalse(log.contains("ERROR"));
    }

    public static String ellipsize(String str) {
        return ELLIPSIZER.ellipsizeMiddle(str);
    }
}
