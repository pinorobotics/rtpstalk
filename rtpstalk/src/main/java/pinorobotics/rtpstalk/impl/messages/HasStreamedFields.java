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
package pinorobotics.rtpstalk.impl.messages;

/**
 * Classes which implement this interface contain streamed fields.
 *
 * <p>Such classes have following requirements:
 *
 * <ul>
 *   <li>All public fields are considered streamed fields.
 *   <li>If class has more than one streamed field it should declare:
 *       <pre>{@code
 * static final List<String> STREAMED_FIELDS = List.of(
 *    // list of all streamed fields ORDERED based on RTPS protocol specification
 * );
 * }</pre>
 * </ul>
 *
 * @see <a href="http://portal2.atwebpages.com/kineticstreamer">kineticstreamer</a>
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public interface HasStreamedFields {}
