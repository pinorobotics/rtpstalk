/*
 * Copyright 2024 rtpstalk project
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

import id.xfunction.util.ImmutableMultiMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.spec.messages.DurabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.ReliabilityQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.StatusInfo;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;

/**
 * Extension of {@link ImmutableMultiMap} which is specific only to RTPS protocol parameters
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class ProtocolParameterMap extends ImmutableMultiMap<ParameterId, Object> {

    public ProtocolParameterMap(ImmutableMultiMap<ParameterId, Object> params) {
        super(params);
    }

    public ProtocolParameterMap(Map.Entry<ParameterId, Object>... entries) {
        super(entries);
    }

    @Override
    public String toString() {
        return super.toJsonString();
    }

    public Optional<ReliabilityQosPolicy.Kind> getReliabilityKind() {
        return getFirstParameter(ParameterId.PID_RELIABILITY, ReliabilityQosPolicy.class)
                .map(ReliabilityQosPolicy::getKind);
    }

    public Optional<DurabilityQosPolicy.Kind> getDurabilityKind() {
        return getFirstParameter(ParameterId.PID_DURABILITY, DurabilityQosPolicy.class)
                .map(DurabilityQosPolicy::getKind);
    }

    public <V> Optional<V> getFirstParameter(ParameterId parameterId, Class<V> classValue) {
        return (Optional<V>) getFirstParameter(parameterId).filter(v -> classValue.isInstance(v));
    }

    public <V> List<V> getParameters(ParameterId parameterId, Class<V> classValue) {
        var values = get(parameterId);
        if (values.isEmpty()) return List.of();
        return classValue.isInstance(values.get(0)) ? (List<V>) values : List.of();
    }

    public boolean hasDisposedObjects() {
        return getFirstParameter(ParameterId.PID_STATUS_INFO, StatusInfo.class)
                .filter(StatusInfo::isDisposed)
                .isPresent();
    }
}
