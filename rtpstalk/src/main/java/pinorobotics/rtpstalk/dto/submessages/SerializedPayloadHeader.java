package pinorobotics.rtpstalk.dto.submessages;

import java.util.Arrays;

import id.xfunction.XJsonStringBuilder;

public class SerializedPayloadHeader {

    public static final int SIZE = 2;

    public RepresentationIdentifier representation_identifier;

    public byte[] representation_options = new byte[SIZE];

    public SerializedPayloadHeader() {

    }

    public SerializedPayloadHeader(RepresentationIdentifier representation_identifier) {
        this.representation_identifier = representation_identifier;
    }

    public SerializedPayloadHeader(RepresentationIdentifier representation_identifier, byte[] representation_options) {
        this.representation_identifier = representation_identifier;
        this.representation_options = representation_options;
    }

    @Override
    public String toString() {
        XJsonStringBuilder builder = new XJsonStringBuilder(this);
        builder.append("representation_identifier", representation_identifier);
        builder.append("representation_options", Arrays.toString(representation_options));
        return builder.toString();
    }
}
