package pinorobotics.rtpstalk.messages;

import id.xfunction.XJsonStringBuilder;

public class UserDataQosPolicy {

    public ByteSequence value;

    public UserDataQosPolicy() {

    }

    public UserDataQosPolicy(ByteSequence value) {
        this.value = value;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("value", value);
        return builder.toString();
    }
}
