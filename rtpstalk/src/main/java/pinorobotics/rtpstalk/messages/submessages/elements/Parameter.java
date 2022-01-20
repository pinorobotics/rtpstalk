package pinorobotics.rtpstalk.messages.submessages.elements;

import id.xfunction.XJsonStringBuilder;

public record Parameter(ParameterId parameterId, Object value) {

    public static final Parameter SENTINEL = new Parameter(ParameterId.PID_SENTINEL, Short.valueOf((short) 0));

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("parameterId", parameterId);
        builder.append("value", value);
        return builder.toString();
    }
}
