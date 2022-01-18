package pinorobotics.rtpstalk.io.exceptions;

import pinorobotics.rtpstalk.dto.submessages.SubmessageKind;

public class UnsupportedSubmessageKindException extends Exception {

    private static final long serialVersionUID = 1L;
    private SubmessageKind kind;

    public UnsupportedSubmessageKindException(SubmessageKind submessageKind) {
        this.kind = submessageKind;
    }

    public SubmessageKind getSubmessageKind() {
        return kind;
    }
}
