package pinorobotics.rtpstalk.io.exceptions;

import pinorobotics.rtpstalk.messages.submessages.elements.ProtocolVersion;

public class UnsupportedProtocolVersion extends Exception {

    private static final long serialVersionUID = 1L;
    private ProtocolVersion version;

    public UnsupportedProtocolVersion(ProtocolVersion version) {
        this.version = version;
    }

    public ProtocolVersion getProtocolVersion() {
        return version;
    }

}
