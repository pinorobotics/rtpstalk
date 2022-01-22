package pinorobotics.rtpstalk.messages.submessages.elements;

import id.xfunction.XJsonStringBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import pinorobotics.rtpstalk.messages.submessages.Payload;

public class ParameterList implements SubmessageElement, Payload {

    public Map<ParameterId, Object> params = new LinkedHashMap<>();

    public ParameterList() {

    }

    public ParameterList(LinkedHashMap<ParameterId, Object> params) {
        this.params = params;
    }

    public ParameterList(List<Entry<ParameterId, Object>> entries) {
        entries.stream().forEach(e -> params.put(e.getKey(), e.getValue()));
    }

    public Map<ParameterId, ?> getParameters() {
        return params;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("params", params);
        return builder.toString();
    }

}
