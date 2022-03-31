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
package pinorobotics.rtpstalk.tests;

import id.xfunction.text.WildcardMatcher;
import java.util.List;
import org.opentest4j.AssertionFailedError;

/** @author lambdaprime intid@protonmail.com */
public class XAsserts {

    public static void assertMatches(List<String> templates, String str) throws AssertionError {
        for (var template : templates) {
            assertMatches(template, str);
        }
    }

    public static void assertMatches(String template, String str) throws AssertionError {
        if (!new WildcardMatcher(template).matches(str))
            throw new AssertionFailedError(
                    String.format("expected template <%s>, actual text <%s>", template, str),
                    template,
                    str);
    }
}
