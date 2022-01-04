package pinorobotics.rtpstalk;

public record RtpsTalkConfiguration(String networkIface) {

	public static final RtpsTalkConfiguration DEFAULT = new RtpsTalkConfiguration("lo");
}
