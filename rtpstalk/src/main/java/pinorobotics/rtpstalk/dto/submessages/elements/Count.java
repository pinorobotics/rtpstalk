package pinorobotics.rtpstalk.dto.submessages.elements;

import id.xfunction.XJsonStringBuilder;

public class Count implements SubmessageElement {

    public int value;

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", value);
        return builder.toString();
    }
}
