package pinorobotics.rtpstalk.dto.submessages;

import id.xfunction.XJsonStringBuilder;
import pinorobotics.rtpstalk.dto.submessages.elements.SubmessageElement;

public class SerializedPayload implements SubmessageElement {

    public SerializedPayloadHeader serializedPayloadHeader;

    public Payload payload;

    public SerializedPayload() {

    }

    public SerializedPayload(SerializedPayloadHeader serializedPayloadHeader, Payload payload) {
        this.serializedPayloadHeader = serializedPayloadHeader;
        this.payload = payload;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("serializedPayloadHeader", serializedPayloadHeader);
        builder.append("payload", payload);
        return builder.toString();
    }

}
