package pinorobotics.rtpstalk.entities;

import java.util.List;
import java.util.Optional;

import id.xfunction.XJsonStringBuilder;

public record ParameterList(List<Parameter> params) implements Payload {
	@Override
	public String toString() {
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("params", params);
		return builder.toString();
	}

	public Optional<Object> findParameter(ParameterId param) {
		return params().stream()
				.filter(p -> p.parameterId() == param)
				.map(Parameter::value)
				.findFirst();
	}
}
