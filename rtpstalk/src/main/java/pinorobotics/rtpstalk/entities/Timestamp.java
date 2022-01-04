package pinorobotics.rtpstalk.entities;

import java.util.Arrays;
import java.util.Objects;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class Timestamp {

	public static Timestamp TIME_ZERO = new Timestamp(0, 0);
	public static Timestamp TIME_INVALID = new Timestamp(0xffffffff, 0xffffffff);
	public static Timestamp TIME_INFINITE = new Timestamp(0xffffffff, 0xfffffffe);
	
	@Streamed
	public int seconds;
	
	/**
	 * Time in sec/2^32
	 */
	@Streamed
	public int fraction;

	public Timestamp() {
		// TODO Auto-generated constructor stub
	}
	
	public Timestamp(int seconds, int fraction) {
		this.seconds = seconds;
		this.fraction = fraction;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(fraction, seconds);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Timestamp other = (Timestamp) obj;
		return fraction == other.fraction && seconds == other.seconds;
	}

	@Override
	public String toString() {
		if (this.equals(TIME_INFINITE)) return "TIME_INFINITE";
		if (this.equals(TIME_INVALID)) return "TIME_INVALID";
		if (this.equals(TIME_ZERO)) return "TIME_ZERO";
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("seconds", seconds);
		builder.append("fraction", fraction);
		return builder.toString();
	}
}
