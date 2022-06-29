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

import id.xfunction.ResourceUtils;
import id.xfunction.text.WildcardMatcher;
import java.util.List;
import java.util.Objects;
import org.opentest4j.AssertionFailedError;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class XAsserts {

    private static final ResourceUtils resourceUtils = new ResourceUtils();

    public static void assertMatchesAll(Class<?> clazz, String resourceWithTemplates, String str)
            throws AssertionError {
        List<String> templates = resourceUtils.readResourceAsList(clazz, resourceWithTemplates);
        for (var template : templates) {
            assertMatches(template, str);
        }
    }

    public static void assertMatches(Class<?> clazz, String resourceTemplate, String str)
            throws AssertionError {
        assertMatches(resourceUtils.readResource(clazz, resourceTemplate), str);
    }

    public static void assertMatches(String template, String str) throws AssertionError {
        if (!new WildcardMatcher(template).matches(str))
            throw new AssertionFailedError(
                    String.format("expected template <%s>, actual text <%s>", template, str),
                    template,
                    str);
    }

    public static void assertEquals(
            Class<?> clazz, String resourceWithExpectedString, String actualString) {
        var expectedString = resourceUtils.readResource(clazz, resourceWithExpectedString);
        if (!Objects.equals(expectedString, actualString))
            throw new AssertionFailedError(
                    String.format("expected <%s>, actual text <%s>", expectedString, actualString),
                    expectedString,
                    actualString);
    }
}
