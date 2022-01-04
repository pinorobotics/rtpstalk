package pinorobotics.rtpstalk.entities;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.kineticstreamer.annotations.Streamed;
import id.xfunction.XJsonStringBuilder;

public class Timestamp {

	public static enum Predefined {
		TIME_ZERO(new Timestamp(0, 0)),
		TIME_INVALID(new Timestamp(0xffffffff, 0xffffffff)),
		TIME_INFINITE(new Timestamp(0xffffffff, 0xfffffffe));
		
		private Timestamp value;

		Predefined(Timestamp value) {
			this.value = value;
		}
		
		public Timestamp getValue() {
			return value;
		}
	}
	
	static Map<Timestamp, Predefined> map = new HashMap<>();
	static {
		for (var t: Predefined.values()) map.put(t.value, t);
	}
	
	private static final Instant epoc = Instant.now();

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
		var predefined = map.get(this);
		if (predefined != null) {
			return predefined.name();
		}
		XJsonStringBuilder builder = new XJsonStringBuilder(this);
		builder.append("seconds", seconds);
		builder.append("fraction", fraction);
		return builder.toString();
	}
	
	public static Timestamp now() {
		return new Timestamp((int) Duration.between(Instant.now(), epoc).toSeconds(), 0);
	}
}
