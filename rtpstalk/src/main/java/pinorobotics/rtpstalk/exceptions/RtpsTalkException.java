package pinorobotics.rtpstalk.exceptions;

/**
 * Generic runtime exception for all <b>rtpstalk</b> operations.
 */
public class RtpsTalkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RtpsTalkException() {
        super();
    }

    public RtpsTalkException(String message) {
        super(message);
    }

    public RtpsTalkException(String fmt, Object... objs) {
        super(String.format(fmt, objs));
    }

    public RtpsTalkException(Exception e) {
        super(e);
    }
}
