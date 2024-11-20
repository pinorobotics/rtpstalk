/*
 * Copyright 2024 pinorobotics
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
package pinorobotics.rtpstalk.impl.spec.transport.io;

import id.xfunction.Preconditions;
import java.util.List;
import pinorobotics.rtpstalk.impl.messages.HasStreamedFields;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class FieldsOrderedByNameProvider {
    public static List<String> readOrderedFieldNames(Class<?> clazz) {
        Preconditions.isTrue(
                HasStreamedFields.class.isAssignableFrom(clazz),
                "Trying to (de)serialize class %s which does not implement HasStreamedFields"
                        + " interface",
                clazz.getSimpleName());
        try {
            var streamedFields = clazz.getDeclaredField("STREAMED_FIELDS");
            Preconditions.equals(
                    List.class,
                    streamedFields.getType(),
                    "HasStreamedFields requirement is not met. Expected streamed fields type to be"
                            + " List.");
            streamedFields.setAccessible(true);
            return (List<String>) streamedFields.get(null);
        } catch (NoSuchFieldException e) {
            return List.of();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(
                    "Could not read list of streamed fields from class %s"
                            .formatted(clazz.getSimpleName()),
                    e);
        }
    }
}
