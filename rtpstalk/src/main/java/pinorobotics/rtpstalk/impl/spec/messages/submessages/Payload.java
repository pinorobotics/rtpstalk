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
package pinorobotics.rtpstalk.impl.spec.messages.submessages;

/**
 * Iface for everything that can be payload in RTPS submessage elements.
 *
 * <p>All Payload classes are subject to (de)serialization.
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public interface Payload {

    RepresentationIdentifier.Predefined getRepresentationIdentifier();

    boolean isEmpty();
}
