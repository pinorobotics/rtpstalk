package pinorobotics.rtpstalk.messages.submessages.elements;

import java.util.List;
import java.util.Optional;
import pinorobotics.rtpstalk.messages.submessages.Payload;
import id.xfunction.XJsonStringBuilder;

public class ParameterList implements SubmessageElement, Payload {

    public List<Parameter> params;

    public ParameterList() {

    }

    public ParameterList(List<Parameter> params) {
        this.params = params;
    }

    public List<Parameter> getParameters() {
        return params;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("params", params);
        return builder.toString();
    }

    public Optional<Object> findParameter(ParameterId param) {
        return params.stream()
                .filter(p -> p.parameterId() == param)
                .map(Parameter::value)
                .findFirst();
    }
}
