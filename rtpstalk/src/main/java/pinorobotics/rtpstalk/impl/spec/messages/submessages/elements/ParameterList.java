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
package pinorobotics.rtpstalk.impl.spec.messages.submessages.elements;

import id.xfunction.XJsonStringBuilder;
import id.xfunction.util.ImmutableMultiMap;
import java.util.Map;
import pinorobotics.rtpstalk.impl.messages.ProtocolParameterMap;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Payload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RepresentationIdentifier;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RepresentationIdentifier.Predefined;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ParameterList implements SubmessageElement, Payload {

    public static final ParameterList EMPTY = new ParameterList();

    private ProtocolParameterMap protocolParams = new ProtocolParameterMap();
    private ImmutableMultiMap<Short, byte[]> userParams = ImmutableMultiMap.of();

    public static final ParameterList ofUserParameters(
            Iterable<Map.Entry<Short, byte[]>> userParams) {
        return ofUserParameters(new ImmutableMultiMap<>(userParams));
    }

    public static final ParameterList ofUserParameters(
            ImmutableMultiMap<Short, byte[]> userParams) {
        var out = new ParameterList();
        out.userParams = userParams;
        return out;
    }

    public static final ParameterList ofProtocolParameters(
            ImmutableMultiMap<ParameterId, Object> protocolParams) {
        var out = new ParameterList();
        out.protocolParams = new ProtocolParameterMap(protocolParams);
        return out;
    }

    public static final ParameterList of(
            ImmutableMultiMap<ParameterId, Object> protocolParams,
            ImmutableMultiMap<Short, byte[]> userParams) {
        var out = new ParameterList();
        out.protocolParams = new ProtocolParameterMap(protocolParams);
        out.userParams = userParams;
        return out;
    }

    public ProtocolParameterMap getProtocolParameters() {
        return protocolParams;
    }

    public ImmutableMultiMap<Short, byte[]> getUserParameters() {
        return userParams;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("params", protocolParams);
        builder.append("userParams", userParams.toJsonString());
        return builder.toString();
    }

    @Override
    public Predefined getRepresentationIdentifier() {
        return RepresentationIdentifier.Predefined.PL_CDR_LE;
    }

    @Override
    public boolean isEmpty() {
        return protocolParams.isEmpty() && userParams.isEmpty();
    }
}
